# Design System Document

## 1. Overview & Creative North Star: "The Ethereal Guardian"
The design system for this emergency protection platform is built upon a Creative North Star we call **"The Ethereal Guardian."** In emergency contexts, traditional UI often feels clinical or alarming. This system rejects that anxiety-inducing aesthetic in favor of an editorial, high-end experience that feels calm, authoritative, and impossibly smooth.

We move beyond the "template" look by utilizing **intentional asymmetry** and **tonal layering**. The interface is not a flat plane; it is a breathable ecosystem of translucent surfaces and deep, resonant tones. We emphasize "Trust" through sophisticated typography and "Elegance" through the restraint of structural lines.

---

## 2. Colors & Atmospheric Depth
Our palette transitions from the deep, protective weight of **Deep Eggplant** to the airy lightness of **Soft Lilac**.

### The "No-Line" Rule
To maintain a premium feel, **1px solid borders are strictly prohibited for sectioning.** Structural boundaries must be defined solely through background shifts. For example, a `surface-container-low` section sitting on a `surface` background creates a sophisticated "zone" without the clutter of lines.

### Surface Hierarchy & Nesting
Treat the UI as physical layers of frosted glass. 
- **Base Layer:** `surface` (#f9f9ff)
- **Secondary Zone:** `surface-container-low` (#f3f3f9) 
- **Floating Prominence:** `surface-container-lowest` (#ffffff) for high-contrast cards.

### The Glass & Gradient Rule
To inject "visual soul," use subtle linear gradients (135°) for primary actions, transitioning from `primary` (#38073b) to `primary_container` (#511f52). For glass elements, apply a `surface` color at 60% opacity with a `backdrop-filter: blur(20px)`.

---

## 3. Typography: Editorial Authority
We utilize a high-contrast scale to create an editorial rhythm.

*   **Display & Headlines (Lexend):** Bold and commanding. Brand headers (e.g., "VAJRA") should use `display-lg` and be centered to act as an anchor of stability.
*   **Body & Labels (Plus Jakarta Sans):** Chosen for its modern, geometric clarity. 
*   **The Intentional Gap:** Use wide tracking (letter-spacing: 0.05em) on `label-sm` elements to create a premium, minimalist feel for data readouts.

| Role | Font | Size | Usage |
| :--- | :--- | :--- | :--- |
| **Display LG** | Lexend Bold | 3.5rem | Brand moments / SOS Triggers |
| **Headline MD** | Lexend SemiBold | 1.75rem | Section headers |
| **Title MD** | Plus Jakarta Sans | 1.125rem | Card titles |
| **Body LG** | Plus Jakarta Sans | 1rem | Primary information |
| **Label MD** | Plus Jakarta Sans | 0.75rem | Metadata and Input labels |

---

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are too "dirty" for this aesthetic. We achieve lift through light and translucency.

*   **The Layering Principle:** Place a `surface-container-lowest` (#ffffff) card on a `surface-container-low` (#f3f3f9) background. This creates a "soft lift" that feels architectural rather than digital.
*   **Ambient Shadows:** For floating emergency buttons, use a shadow with a 40px blur, 10% opacity, tinted with the `primary` color (#38073b). Never use pure black for shadows.
*   **The Ghost Border Fallback:** If a boundary is required for accessibility, use the `outline_variant` (#d2c2cd) at **15% opacity**. It should be felt, not seen.
*   **Glassmorphism:** Navigation bars must be pill-shaped floating elements using `surface_bright` at 70% opacity with a heavy blur. This allows content to bleed through, keeping the app feeling "breathable."

---

## 5. Components: Fluid Primitives

### Pill Buttons (Primary)
*   **Shape:** `rounded-full` (9999px).
*   **Style:** Gradient fill (`primary` to `primary_container`).
*   **State:** On press, scale to 98% to simulate physical depth.
*   **Padding:** `spacing-4` (1.4rem) horizontal to ensure a luxury "wide" footprint.

### Floating Glass Cards
*   **Shape:** `rounded-lg` (2rem).
*   **Constraint:** **Forbid divider lines.** Use `spacing-6` (2rem) of vertical whitespace to separate card sections. Use `surface-container-highest` (#e2e2e8) for subtle internal grouping.

### Modern Toggle Switches
*   **Track:** `secondary_container` when off; `primary` when on.
*   **Thumb:** Always `surface-container-lowest` (#ffffff).
*   **Visual:** No borders; use a soft ambient shadow on the thumb for tactile realism.

### Circular Status Badges
*   Used for "Live" monitoring. Use `secondary` (#7e4d7d) with a pulsing outer ring (20% opacity) to denote activity without using aggressive "error red" unless absolutely critical.

---

## 6. Do’s and Don’ts

### Do
*   **Do** use asymmetrical layouts for hero sections (e.g., text left-aligned, status icon floating top-right).
*   **Do** use `spacing-10` or `spacing-12` for generous margins; air is luxury.
*   **Do** use "Soft Lilac" (#ecd4ea) for backgrounds of non-critical alert chips to maintain the premium palette.

### Don’t
*   **Don't** use 100% opaque, high-contrast borders. It breaks the "Ethereal" illusion.
*   **Don't** use standard material "Elevation 1-5" shadows. Use our Tonal Layering or Ambient Shadow rules.
*   **Don't** clutter the screen. If a piece of data isn't essential for the current emergency state, hide it within a progressive disclosure layer.
*   **Don't** use sharp corners. Stick to the `Roundedness Scale` (Default: 1rem) to maintain a soft, approachable persona.