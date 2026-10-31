# 🧱 Modern Block Registration (`references/blocks.md`)

Source: `D:\laragon\www\favorites-dev\wp-content\plugins\favorites\tangible-favorites.php`

---

## High-Performance Metadata Collection (WordPress 6.7 / 6.8+)

In modern WordPress (6.7+), block types are registered via **`blocks-manifest.php`** using metadata collections rather than scanning disk directories one-by-one.

### Canonical Pattern
```php
/**
 * Registers blocks using `blocks-manifest.php` with backward compatibility.
 *
 * @see https://make.wordpress.org/core/2025/03/13/more-efficient-block-type-registration-in-6-8/
 * @see https://make.wordpress.org/core/2024/10/17/new-block-type-registration-apis-to-improve-performance-in-wordpress-6-7/
 */
function tangible_plugin_blocks_init(): void {
  $blocks_dir     = __DIR__ . '/assets/build/blocks/';
  $manifest_file  = $blocks_dir . 'blocks-manifest.php';

  if ( ! file_exists( $manifest_file ) ) return;

  // WP 6.8+
  if ( function_exists( 'wp_register_block_types_from_metadata_collection' ) ) {
    wp_register_block_types_from_metadata_collection( $blocks_dir, $manifest_file );
    return;
  }

  // WP 6.7
  if ( function_exists( 'wp_register_block_metadata_collection' ) ) {
    wp_register_block_metadata_collection( $blocks_dir, $manifest_file );
  }

  // Fallback for older WP versions
  $manifest_data = require $manifest_file;
  foreach ( array_keys( $manifest_data ) as $block_type ) {
    register_block_type( $blocks_dir . $block_type );
  }
}

add_action( 'init', 'tangible_plugin_blocks_init' );
```
