package me.fzzyhmstrs.amethyst_core.scepter_util.augments

/**
 * no-method interfaces used to organize augments. This organization is used by Modifier Predicates so some modifiers can only trigger on a certain subset of augments.
 *
 * For example, if you have a "Soul Stealer" modifier, it may only be applicable to SoulAugment spells, spells that are tpyically linked to blood magic, armor penetration, and other effects that "go straight to the soul"
 */

/**
 * Augments related to fire and explosions
 */
interface FireAugment

/**
 * Augments related to lightning, sparks, electricity
 */
interface LightningAugment

/**
 * Augments related to healing and other beneficial effects
 */
interface HealerAugment

/**
 * Augments related to placing or modifiying blocks in the world
 */
interface BuilderAugment

/**
 * Augments related to water or the ocean, or that have special effects in water
 */
interface OceanicAugment

/**
 * Augments related to movement, scouting, and observation
 */
interface TravelerAugment

/**
 * Augments related to Blood/Spirit magic.
 */
interface SoulAugment