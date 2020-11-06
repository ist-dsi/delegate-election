angular.module('delegados').controller('voteCtrl', ['$rootScope', '$scope', '$http', '$log', function(rc, sc, http,log) {
		sc.students = [];
		
		sc.selected = '';
		
		rc.voto = "";
		
		sc.selection = function() {
//			log.log(sc.selected);
			if(sc.selected == 'other') {
				for(var i = 0; i < sc.students.length; i++) {
				    if (sc.students[i].username == sc.otherSelected) {
				        name = sc.students[i].name;
				        break;
				    }
				}
				sc.selectionResult = name + ' (' + sc.otherSelected +')';
			}
			else if(sc.selected == 'nil')
				sc.selectionResult = 'branco';
			else {
				for(var i = 0; i < sc.candidatos.length; i++) {
				    if (sc.candidatos[i].username == sc.selected) {
				        name = sc.candidatos[i].name;
				        break;
				    }
				}
				sc.selectionResult = name + ' (' + sc.selected +')';
			}
		}
		
		sc.vote = function() {
			var username = sc.selected;
			if (sc.selected == 'other') {
				username = sc.otherSelected;
			}
			http.post('students/'+rc.credentials.username+'/degrees/'+rc.degree.id+'/votes', username)
			.success(function(data) { 
				sc.feedback = true;
				rc.voted = true;
				rc.voto = data;
//				log.log(rc.voto);
			});
		}
		
		sc.loadStudents = function(query) {
			if(query != null && query != '' && query.length >= 2){
				http.get('degrees/'+rc.degree.id+'/years/'+rc.degree.curricularYear+'/students?begins='+query)
				.success(function(data) { 
					sc.students = data;
				});
			}
		}
	}
]);
