# 📦 Tangible Object Master Reference (`references/object.md`)

Source: `D:\laragon\www\tobet-dev\wp-content\plugins\tobet-features\includes\objects\`

---

## 1. DataView & EditorLayout Pattern

`Tangible\DataView` generates modern, accessible WordPress admin CRUD screens with automatic field rendering, tabs, and sidebars.

### Architecture Structure
```text
includes/objects/
├── Registry.php             # FieldTypeRegistry definition
├── Renderer.php             # Custom TangibleFieldsRenderer instance
├── index.php                # Views registry & admin_menu registration
└── <entity>/
    ├── view.php             # Entity fields definition array
    └── layout.php           # Tabs & sidebar layout definition
```

### Canonical `includes/objects/index.php`
```php
<?php declare( strict_types=1 );

namespace Tangible\Plugin\Objects;

defined( 'ABSPATH' ) or die();

use Tangible\DataView\DataView;
use Tangible\EditorLayout\Layout;
use Tangible\EditorLayout\Sidebar;

function objects(): array {
  return [
    'diocese' => require __DIR__ . '/diocese/view.php',
  ];
}

function views(): array {
  static $views = false;
  if ( $views !== false ) return $views;

  $registry = require __DIR__ . '/Registry.php';

  $views = [];
  foreach ( objects() as $name => $config ) {
    // CRITICAL: Each view gets its own fresh Renderer instance
    $view = new DataView( $config, $registry );
    $view->set_renderer( require __DIR__ . '/Renderer.php' );
    $views[ $name ] = $view;
  }

  return $views;
}

add_action( 'admin_menu', function(): void {
  foreach ( views() as $name => $view ) {
    $view->register();
    $view->set_layout( function( Layout $layout ) use ( $name, $view ) {
      require __DIR__ . '/' . $name . '/layout.php';

      $layout->sidebar( function( Sidebar $side_bar ) use ( $view ) {
        if ( $view->get_config()->is_singular() ) {
          $side_bar->actions( [ 'save' ] );
          return;
        }

        ( $_GET['action'] ?? '' ) === 'create'
          ? $side_bar->actions( [ 'create' ] )
          : $side_bar->actions( [ 'save', 'delete' ] );
      } );
    } );
  }
} );
```

---

## 2. Layout Definition (`layout.php`)

```php
<?php declare( strict_types=1 );

use Tangible\EditorLayout\Tabs;
use Tangible\EditorLayout\Tab;

defined( 'ABSPATH' ) or die();

$layout->tabs( function( Tabs $tabs ) {
  $tabs->tab( 'General', function( Tab $tab ) {
    $tab->field( 'title' );
    $tab->field( 'description' );
  } );
  $tabs->tab( 'Settings', function( Tab $tab ) {
    $tab->field( 'status' );
    $tab->field( 'items' );
  } );
} );
```

---

## 3. Custom Post Type Repeater Gotcha

When storing an entity in Custom Post Type metadata:
- CPT storage encodes the whole entity into one postmeta JSON value.
- WordPress's `update_post_meta()` runs `wp_unslash()`, which strips escaping from nested JSON strings, corrupting repeater data.
- **Solution:** Always register repeater fields as a decoded PHP array (`DataSet::TYPE_ARRAY`) with an array sanitizer, so it nests cleanly without corrupting quotes.
