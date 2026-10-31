---
name: tangible
description: >-
  Official Tangible Framework & Fields Master Reference. Provides deep architectural rules,
  API syntax, and code examples for tangible/framework, tangible/fields, tangible/object,
  @tangible/ui, @tangible/roller, and modern WP block registration. Use whenever writing,
  debugging, or reviewing code for Tangible WordPress plugins or when the user invokes @tangible.
---

# 🧩 Skill: Tangible Framework & Fields Master Guide (`@tangible`)

This skill is the single source of truth for Tangible's proprietary development ecosystem, extracted directly from official Tangible codebases (`tangible-fields`, `favorites`, `tobet-features`, `tangible-sitesync`).

---

## 🗺️ Tangible Ecosystem Architecture

Tangible plugins are built on 5 complementary layers:

| Layer | Repository / Package | Purpose |
| :--- | :--- | :--- |
| **1. Framework** | `tangible/framework` (`vendor/tangible/framework`) | Core bootstrap, `register_plugin()`, `tangible\ajax`, `tangible\api`, background queues, settings tabs |
| **2. Fields** | `tangible/fields` (`vendor/tangible/fields`) | 25+ React-based form field types (`window.tangibleFields.render`), repeaters, conditional logic |
| **3. Object** | `tangible/object` (Composer: `tangible/object`) | `DataView`, `EditorLayout`, `FieldTypeRegistry`, and `TangibleFieldsRenderer` |
| **4. UI Design System** | `@tangible/ui` (NPM: `@tangible/ui`) | 46 accessible React components (`Field`, `TextInput`, `SecretInput`, `Button`, `Modal`, `Table`, `Tabs`) + `.tui-interface` scope & `--tui-*` tokens |
| **5. Toolchain** | `@tangible/roller` (NPM: `@tangible/roller`) | Bundler & CLI configured via `tangible.config.js` (`roll dev`, `roll build`, `roll archive`) |

---

## ⚡ Canonical Plugin Bootstrap (`<plugin-slug>.php`)

Every modern Tangible plugin must follow this exact bootstrap:

```php
<?php
/**
 * Plugin Name: Tangible Example
 * Version: 0.1.0
 * License: GPLv2 or later
 * Text Domain: tangible-example
 * Requires PHP: 8.2
 */

declare( strict_types = 1 );

defined( 'ABSPATH' ) || exit;

define( 'TANGIBLE_EXAMPLE_VERSION', '0.1.0' );

require_once __DIR__ . '/vendor/tangible/framework/index.php';
require_once __DIR__ . '/vendor/tangible/fields/index.php';
// Optional: require_once __DIR__ . '/vendor/autoload.php'; (when using tangible/object or PSR-4)

function tangible_example( $instance = false ) {
  static $plugin;
  return $plugin ? : ( $plugin = $instance );
}

add_action( 'plugins_loaded', function () {

  // 1. Activation guard: prevent short-circuiting during sandbox scraping
  if ( defined( 'WP_SANDBOX_SCRAPING' ) ) return;

  // 2. Register with Tangible Framework
  $plugin = tangible_example( tangible\framework\register_plugin( [
    'name'           => 'tangible-example',
    'title'          => 'Tangible Example',
    'setting_prefix' => 'tangible_example',
    'version'        => TANGIBLE_EXAMPLE_VERSION,
    'file_path'      => __FILE__,
    'base_path'      => __DIR__,
    'url'            => plugins_url( '/', __FILE__ ),
    'assets_url'     => plugins_url( '/assets', __FILE__ ),
  ] ) );

  // 3. Optional DB migrations
  // ( $plugin->db = require_once __DIR__ . '/includes/db.php' )->maybe_install();

  // 4. Require main includes entrypoint
  require_once __DIR__ . '/includes/index.php';

}, 10 );
```

---

## 📚 Deep Reference Manuals

Read the dedicated reference files in `references/` for full syntax and examples:

1. **[`references/fields.md`](./references/fields.md)** — All 25+ field types, arguments, repeaters (`advanced`, `bare`, `block`, `table`), JS `tangibleFields.render()`, and repeater JS callbacks.
2. **[`references/framework.md`](./references/framework.md)** — `framework\register_plugin_settings()`, `tangible\ajax\add_action()`, `tangible\api`, async actions, and background queue.
3. **[`references/object.md`](./references/object.md)** — `Tangible\DataView\DataView`, `EditorLayout\Layout`, `Tabs`, `Sidebar`, `FieldTypeRegistry`, and per-view `TangibleFieldsRenderer`.
4. **[`references/blocks.md`](./references/blocks.md)** — Modern WordPress 6.7/6.8+ block registration via `blocks-manifest.php` (`wp_register_block_types_from_metadata_collection`).
5. **[`references/roller.md`](./references/roller.md)** — `tangible.config.js` syntax, `react: 'wp'`, `importToGlobal`, and archive packaging.
6. **[`references/ui.md`](./references/ui.md)** — `@tangible/ui` React component guide (`Field`, `TextInput`, `SecretInput`, `Button`, `Modal`, `Tabs`, `Table`) with token mapping and callback conventions.
