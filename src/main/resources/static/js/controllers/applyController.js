angular.module('delegados').controller('applyCtrl', ['$rootScope', '$scope', '$http', '$log', function(rc, sc, http, log) {
		
		sc.apply = function() {
			http.post('degrees/'+rc.degree.id+'/years/'+rc.degree.curricularYear+'/candidates', rc.credentials.username)
			.success(function(data) { 
				rc.applied = true;
				http.get('degrees/'+rc.degree.id+'/years/'+rc.degree.curricularYear+'/candidates')
				.success(function(data) { 
					rc.candidatos = data;
					sc.feedback = true;
				});
			});
		};
		
		sc.unapply = function() {
			http.delete('degrees/'+rc.degree.id+'/years/'+rc.degree.curricularYear+'/candidates/'+rc.credentials.username)
			.success(function(data) { 
				rc.applied = false;
//				log.log(rc.applied);
				http.get('degrees/'+rc.degree.id+'/years/'+rc.degree.curricularYear+'/candidates')
				.success(function(data) { 
					rc.candidatos = data;
//					log.log(rc.candidatos);
					sc.feedback = false;
				});
			});
		};
}]);
