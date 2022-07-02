package me.fzzyhmstrs.amethyst_core.registry

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.Model
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.EntityModelLoader
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.Item
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import net.minecraft.util.Identifier
import java.util.*
import java.util.function.Consumer
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

/**
 * Registry for custom item models. Use this if you want to implement items that appear different in different [ModelTransformation.Mode].
 *
 * The easy example of an item that does this is the Minecraft Trident. In inventory, it appears as a pixel art icon, but in hand and in third person, it appears as a rendered entitty.
 *
 * This registry provides methods for handling both of those situations (rendering differently per Mode, and rendering as an entity)
 */
@Environment(value = EnvType.CLIENT)
object ItemModelRegistry: SynchronousResourceReloader {

    private val fallbackId = ModelIdentifier("minecraft:trident_in_hand#inventory")
    private val modelIdMap: HashMap<Item, ModelIdentifierPerModes> = HashMap()
    private val entityModelMap: HashMap<Item,CustomItemEntityModelLoader> = HashMap()
    private val entityModelLoader: EntityModelLoader by lazy { MinecraftClient.getInstance().entityModelLoader }


    override fun reload(manager: ResourceManager) {
        entityModelMap.forEach {
            it.value.reload()
        }
    }

    internal fun registerAll(){
        registerReloader()
    }

    /**
     * base registration method for assigning different models to different view modes. Uses a [ModelIdentifierPerModes] instance to define what models you want to appear when.
     *
     * if you are registering an item entity, the model json corresponding to the entity must use builtin/entity as the model parent
     */
    fun registerItemModelId(item: Item, models: ModelIdentifierPerModes){
        if (modelIdMap.containsKey(item)){
            throw IllegalStateException("Item ${item.name} already present in ItemModelRegistry")
        }
        modelIdMap[item] = models
    }

    /**
     * if you want your item to appear as a rendered entity in one or more render modes, register that information here. This method wraps the needed underlying registries into one method call
     *
     * [item]: the registered item to define an entity model for.
     *
     * [renderer]: A [BuiltinItemRendererRegistry.DynamicItemRenderer] used by Fabric for rendering the entity. Look at Amethyst Imbuements Glistering Trident Item Model Renderer for an example implementation.
     *
     * [layer]: The model layer for the Entity.
     *
     * [classType]: the Entity model java class. For example the vanilla TridentEntityModel.class or TridentEntityModel::class.java
     */
    fun registerItemEntityModel(item: Item , renderer: BuiltinItemRendererRegistry.DynamicItemRenderer, layer: EntityModelLayer , classType : Class<out Model>){
        entityModelMap[item] = CustomItemEntityModelLoader(layer, classType)
        BuiltinItemRendererRegistry.INSTANCE.register(item, renderer)
    }

    private fun registerReloader(){
        ClientLifecycleEvents.CLIENT_STARTED.register{
                client: MinecraftClient -> (client.resourceManager as ReloadableResourceManagerImpl).registerReloader(this)
        }
    }

    fun itemHasCustomModel(item: Item): Boolean{
        return (modelIdMap.containsKey(item))
    }

    fun getModel(item: Item, mode: ModelTransformation.Mode): ModelIdentifier{
        return modelIdMap[item]?.getIdFromMode(mode) ?: fallbackId
    }

    /**
     * returns a [CustomItemEntityModelLoader] for the specified item that can be used to fetch baked models for rendering with a [BuiltinItemRendererRegistry.DynamicItemRenderer]
     */
    fun getEntityModelLoader(item: Item): CustomItemEntityModelLoader{
        return entityModelMap[item]?:throw NoSuchElementException("Item ${item.name} not present in model loader registry.")
    }

    /**
     * Used to tell the registry which models to use for which [ModelTransformation.Mode]
     *
     * [defaultId]: The fallback [ModelIdentifier]. If a specific replacement model ID isn't called out for a Mode, this is used.
     *
     * This class uses a Builder pattern, so the contained with_ functions can be called in series and inline.
     *
     * For all builder methods, set needsRegstration to true if this is the first time you've added this model identifier to the PerModes instance. This will register the special model in the Fabric ModelLoading registry.
     */
    class ModelIdentifierPerModes(private val defaultId: ModelIdentifier){
        private val modeMap: EnumMap<ModelTransformation.Mode,ModelIdentifier> = EnumMap(ModelTransformation.Mode::class.java)

        /**
         * define a model for a specifically chosen transformation mode.
         */
        fun with(mode: ModelTransformation.Mode, modelId: ModelIdentifier, needsRegistration: Boolean = false): ModelIdentifierPerModes{
            if (needsRegistration){
                registerIdWithModalLoading(modelId)
            }
            modeMap[mode] = modelId
            return this
        }

        /**
         * use this for defining the model for all inventory related modes as well as when the item is floating on the ground.
         *
         * An example usage might be an item that has a specific GUI icon, but shows a more complicated sprite in hand/third person.
         */
        fun withGuiGroundFixed(modelId: ModelIdentifier, needsRegistration: Boolean = false): ModelIdentifierPerModes{
            if (needsRegistration){
                registerIdWithModalLoading(modelId)
            }
            modeMap[ModelTransformation.Mode.GUI] = modelId
            modeMap[ModelTransformation.Mode.FIXED] = modelId
            modeMap[ModelTransformation.Mode.GROUND] = modelId
            return this
        }

        /**
         * defines the model for both first person view modes
         */
        fun withFirstHeld(modelId: ModelIdentifier, needsRegistration: Boolean = false): ModelIdentifierPerModes {
            if (needsRegistration){
                registerIdWithModalLoading(modelId)
            }
            modeMap[ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND] = modelId
            modeMap[ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND] = modelId
            return this
        }

        /**
         * defines the model for both third person view modes
         */
        fun withThirdHeld(modelId: ModelIdentifier, needsRegistration: Boolean = false): ModelIdentifierPerModes {
            if (needsRegistration){
                registerIdWithModalLoading(modelId)
            }
            modeMap[ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND] = modelId
            modeMap[ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND] = modelId
            return this
        }

        /**
         * defines a model for all held modes, first and htird person. This is what Amethyst Imbuement uses to define the special entity model when the Glistering Trident is being held or viewed by another player/in F5 mode.
         *
         * This is the "opposite" set of modes compared to [withGuiGroundFixed]
         */
        fun withHeld(modelId: ModelIdentifier, needsRegistration: Boolean = false): ModelIdentifierPerModes{
            if (needsRegistration){
                registerIdWithModalLoading(modelId)
            }
            modeMap[ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND] = modelId
            modeMap[ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND] = modelId
            modeMap[ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND] = modelId
            modeMap[ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND] = modelId
            return this
        }

        /**
         * return a specific model identifier based on the imput view mode.
         */
        fun getIdFromMode(mode: ModelTransformation.Mode): ModelIdentifier{
            return modeMap[mode] ?: defaultId
        }

        companion object{
            fun registerIdWithModalLoading(id: ModelIdentifier){
                ModelLoadingRegistry.INSTANCE.registerModelProvider { _: ResourceManager?, out: Consumer<Identifier?> ->
                    out.accept(id)
                }
            }
        }
    }

    /**
     * Container storing a custom item entity model that automatically reloads with the client.
     *
     * call [getModel] to retrieve the model stored within.
     */
    class CustomItemEntityModelLoader(private val layer: EntityModelLayer, private val classType: Class<out Model>){

        private lateinit var model: Model

        internal fun reload(){
            model = internalReload()
        }

        fun getModel(): Model{
            if (!this::model.isInitialized){
                model = internalReload()
            }
            return model
        }

        private fun internalReload(): Model{
            val constructor = classType.getConstructor(ModelPart::class.java)
            val modelPart = entityModelLoader.getModelPart(layer)
            return constructor.newInstance(modelPart)
        }
    }
}