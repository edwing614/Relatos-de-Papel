function fn() {
  var config = {
    baseUrl: karate.properties['baseUrl'] || 'http://localhost:8081',
    booksPath: '/libros'
  };
  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 10000);
  return config;
}
