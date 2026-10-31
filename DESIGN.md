---
name: emuLib
description: Shared desktop controls for emuStudio's instrument console.
colors:
  cpu-running: '#009933'
  table-row-even: '#f1f5fa'
  table-row-odd: '#ffffff'
  table-grid: '#d9d9d9'
typography:
  body:
    fontFamily: Dialog, sans-serif
    fontSize: 12px
    fontWeight: 400
  label:
    fontFamily: SansSerif, sans-serif
    fontSize: 12px
    fontWeight: 700
  data:
    fontFamily: Monospaced, monospace
    fontSize: 12px
    fontWeight: 400
  data-bold:
    fontFamily: Monospaced, monospace
    fontSize: 12px
    fontWeight: 700
  data-large:
    fontFamily: Monospaced, monospace
    fontSize: 18px
    fontWeight: 700
components:
  button:
    typography: '{typography.body}'
  input:
    typography: '{typography.data}'
  section:
    typography: '{typography.label}'
  table-row-even:
    backgroundColor: '{colors.table-row-even}'
  table-row-odd:
    backgroundColor: '{colors.table-row-odd}'
  cpu-status:
    textColor: '{colors.cpu-running}'
---

# Design System: emuLib

## Overview

**Creative North Star: "Instrument Console"**

Technical, focused, compact, and orderly. Shared controls are precise, familiar,
and approachable, supporting the same desktop language as emuStudio. No additional
visual exclusions were established.

emuLib supplies constants, icons, and UI contracts. The host implements `GUI`,
available through `ApplicationApi.getGUI()`, and owns look-and-feel styling.
This captures the existing system under [#123](https://github.com/emustudio/emuLib/issues/123).

**Key Characteristics:**

- Compact desktop controls with explicit labels and visible state.
- Logical dialog fonts for controls; monospaced fonts for machine data.
- Mostly flat surfaces organized by titled borders and tonal layers.
- Host-owned theme styling alongside explicitly fixed SDK colors.

Sources: `src/main/java/net/emustudio/emulib/runtime/ui/` (`Constants.java`,
`GUI.java`, `StandardIcons.java`, and `components/DialogBase.java`); host styling
in `../emuStudio/DESIGN.md` and the sibling application's `EmuStudioGui` class.
Current factory sources were rendered offscreen in IntelliJ, Light, and Dark at
1×; a website settings screenshot supplied window context. This does not establish
native window behavior, cross-platform parity, or accessibility conformance.

The [DESIGN.md format](https://raw.githubusercontent.com/google-labs-code/design.md/main/docs/spec.md)
provides the token structure. Here `px` represents Java user-space units at 1×,
not fixed physical pixels. Java sources govern implementation; logical fonts
resolve per OS. Frontmatter contains SDK constants, not a host theme snapshot.

## Colors

The fixed palette describes machine state and table defaults. Ordinary control
surfaces, selection, focus, and disabled colors come from the host look and feel.

| Character | Token | Existing role |
| --- | --- | --- |
| Running Green | `cpu-running` | CPU run-state foreground available to status UIs. |
| Pale Blue Row | `table-row-even` | Named even-row table default. |
| Paper Row | `table-row-odd` | Named odd-row table default. |
| Fine Gray Grid | `table-grid` | Table grid constant; not a requirement to show grid lines. |

**The Host Theme Rule.** Obtain ordinary controls from the host GUI service;
do not replace theme roles with sampled colors. Fixed constants do not imply
automatic Dark-theme adaptation. The host debugger renderer currently applies
the named row constants with its own parity mapping and current-instruction color.

Sidecar tonal ramps are preview aids, not implemented palette steps.

## Typography

`body` maps to `FONT_COMMON`; `label` to `FONT_TITLE_BORDER`; `data`, `data-bold`,
and `data-large` to the three shared monospaced constants. The label role describes
titled borders, not every ordinary label.

The host uses regular dialog text for controls and bold emphasis for important
labels and primary buttons. `labelTitle` derives bold text two units larger than
its base label font. No shared display scale or bundled code-editor font exists
in emuLib.

**The Technical Type Rule.** Keep machine data monospaced and surrounding controls
in the host's dialog typography.

## Layout

Use host factories for panels, titled sections, toolbars, split panes, and scroll
panes. The current host uses MigLayout; constraints are supplied by callers.
Horizontal panels use platform dialog insets. There is no SDK-wide spacing scale,
web grid, or breakpoint system.

The default text field has 20 columns. The current host styles tables with no
grid, zero intercell spacing, and row height `max(existingHeight, 22) + 4`; the
captured samples were 26 units. List borders have two-unit insets. These are host
implementation details, not additional emuLib constants.

## Elevation & Depth

Mostly flat: titled borders and tonal layers organize groups. Window and popup
depth belongs to the host look and feel. emuLib defines no shadow or motion scale.

## Shapes

Control shape and corners belong to the host. emuStudio currently configures
button, component, and text-component arc diameters of eight units, equivalent
to a four-unit radius at 1×. This is not an SDK rounding token.

## Components

### Buttons and toolbars

Precise, familiar, approachable. Named actions use ordinary buttons; toolbar
factories accept actions, icons, and tooltips. The toolbar contract specifies
non-floating, rollover-enabled, borderless toolbars. Hover, focus, pressed, and
disabled painting comes from the host. `buttonMakePrimary` marks a button
default-capable and bold in the current host; the root pane selects the default.

### Fields and sections

Text fields and read-only text areas use the shared monospaced role in emuStudio.
Use titled sections for related controls. Labels, validation, and field-specific
behavior remain the caller's responsibility; no universal error style is defined.

### Tables and split panes

`styleTable` delegates visual treatment to the host. Table constants are available
to custom renderers but do not prescribe every table's selection or row colors.
Split panes are borderless, one-touch expandable, and continuously laid out.

### Dialogs and icons

`DialogBase` packs content, centers on its parent, and disposes on close. Escape
closes by default; subclasses may opt out for emulated-keyboard use.
`StandardIcons` supplies shared resource paths. Action icons are 22×22 with
44×44 `@2x` variants; use the icon-loading helpers to preserve HiDPI resources.
Logo assets are separate from action-icon sizing.

The sidecar contains portable HTML/CSS illustrations of host-rendered patterns.
Their light-theme styling is illustrative, not Swing behavior or pixel parity.

## Do's and Don'ts

### Do

- Do use the host GUI service for shared desktop controls.
- Do distinguish fixed SDK colors from theme-owned control styling.
- Do preserve monospaced machine data and multi-resolution icons.
- Do check focus, disabled, and selected states in the host's supported themes.

### Don't

- Don't promote host theme samples into SDK constants.
- Don't assume custom table renderers automatically adapt to Dark.
- Don't apply generated tonal ramps as implemented palette tokens.
- Don't impose a web layout or spacing system on these desktop contracts.
