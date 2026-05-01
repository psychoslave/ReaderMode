// Test JSX with double-quoted attributes
<Typography variant="body2" component="span" sx={{ fontWeight: 700 }}>
  Hello World
</Typography>

// Single and double quotes mixed
<Button color='primary' text="Click me">
  Button Text
</Button>

// Nested strings
<div className="container" data-test='test-id'>
  <span id="title" title="Tooltip text">
    Content
  </span>
</div>

