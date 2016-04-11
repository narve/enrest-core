var eksApp = angular.module('EksApp', ['ngRoute'])

.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'partials/phone-list.html',
        controller: 'EksCtrl'
      }).
      when('/phones', {
        templateUrl: 'partials/phone-list.html',
        controller: 'EksCtrl'
      }).
      when('/phones/:phoneId', {
        templateUrl: 'partials/phone-detail.html',
        controller: 'EksCtrl'
      }).
      otherwise({
        redirectTo: '/phones'
      });
  }])
  .controller('EksCtrl', function ($scope) {
      $scope.phones = [
        {'name': 'Nexus S', id:1,
         'snippet': 'Fast just got faster with Nexus S.'},
        {'name': 'Motorola XOOM™ with Wi-Fi', id:2,
         'snippet': 'The Next, Next Generation tablet.'},
        {'name': 'MOTOROLA XOOM™',id:3,
         'snippet': 'The Next, Next Generation tablet.'}
      ];
});


