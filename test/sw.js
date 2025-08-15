// service worker sw.js
self.addEventListener('push', function(event) {
  let data = {};
  try {
    data = event.data.json();
  } catch (e) {
    console.error('Push event but no data');
  }

  const options = {
    body: data.body,
    icon: data.icon,
    image: data.image,
    badge: data.badge,
    data: data.url
  };

  event.waitUntil(
    self.registration.showNotification(data.title, options)
  );
});
