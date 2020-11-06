angular.module('delegados', ['ngRoute', 'pascalprecht.translate', 'ui.bootstrap']).
	config(function($httpProvider, $routeProvider, $locationProvider) {
		
		$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
}).config(function($translateProvider) {
		$translateProvider.useStaticFilesLoader({
		  prefix: '/languages/',
		  suffix: '.json'
		});
		//$translateProvider.useLocalStorage();/
		$translateProvider.preferredLanguage('ptPT');
});
