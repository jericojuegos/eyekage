# ⚙️ Tangible Framework Master Reference (`references/framework.md`)

Source: `vendor/tangible/framework`

---

## 1. Plugin Registration & Activation Guard

Always guard `plugins_loaded` against WordPress sandbox scraping to prevent premature loading:

```php
use tangible\framework;

add_action( 'plugins_loaded', function() {
  if ( defined( 'WP_SANDBOX_SCRAPING' ) ) return;

  $plugin = framework\register_plugin( [
    'name'           => 'my-plugin-slug',
    'title'          => 'My Plugin Title',
    'setting_prefix' => 'my_plugin_prefix',
    'version'        => '0.1.0',
    'file_path'      => __FILE__,
    'base_path'      => __DIR__,
    'url'            => plugins_url( '/', __FILE__ ),
    'assets_url'     => plugins_url( '/assets', __FILE__ ),
  ] );
} );
```

---

## 2. Tangible AJAX Handlers (`tangible\ajax`)

Tangible AJAX automatically verifies nonces and handles JSON wrapping:

### Server (PHP):
```php
use tangible\ajax;

// Register action (logged-in users only by default)
ajax\add_action( 'save_settings', function( array $data ) {
  if ( ! current_user_can( 'manage_options' ) ) {
    throw new \Exception( 'Permission denied.' );
  }

  $name = sanitize_text_field( (string) ( $data['name'] ?? '' ) );
  if ( $name === '' ) {
    throw new \InvalidArgumentException( 'Name is required.' );
  }

  update_option( 'my_plugin_name', $name );

  return [ 'success' => true, 'name' => $name ];
} );

// Enqueue Tangible AJAX script for frontend or admin
ajax\enqueue();
```

### Browser (JS):
```javascript
const { ajax } = window.Tangible

ajax('save_settings', { name: 'My New Name' })
  .then(response => {
    console.log('Saved:', response)
  })
  .catch(error => {
    console.error('Save failed:', error)
  })
```

---

## 3. Plugin Settings Screen (`framework\register_plugin_settings`)

```php
use tangible\framework;

framework\register_plugin_settings( $plugin, [
  'css' => $plugin->assets_url . '/build/admin.min.css',
  'js'  => $plugin->assets_url . '/build/admin.min.js',
  'title_callback' => function() use ( $plugin ) {
    ?>
    <img src="<?= esc_url( $plugin->assets_url . '/images/logo.png' ) ?>" alt="" width="32">
    <?= esc_html( $plugin->title ) ?>
    <?php
  },
  'tabs' => [
    'general' => [
      'title'    => __( 'General', 'text-domain' ),
      'callback' => function() use ( $plugin ) {
        require_once __DIR__ . '/tabs/general.php';
      },
    ],
    'advanced' => [
      'title'    => __( 'Advanced', 'text-domain' ),
      'callback' => function() use ( $plugin ) {
        require_once __DIR__ . '/tabs/advanced.php';
      },
    ],
  ],
] );
```

---

## 4. Background Queue & Async Actions

### Background Task Queue (Bulk Syncs & Processing)
```php
use tangible;

$queue = tangible\register_background_queue( [
  'name'     => 'sync_posts_queue',
  'callback' => function( $item ) {
    // Process single item
    // return false on failure to retry, or true on success
  },
  'complete' => function() {
    // Fired when entire queue is finished
  },
  'error'    => function( $item, $error ) {
    // Log item error
  },
] );

// Push items to queue
$queue->add_items( [ $item1, $item2, $item3 ] );
```
