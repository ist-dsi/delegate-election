angular.module('delegados').controller('removeModalCtrl', ['$rootScope', '$scope', '$http', '$log', 'periodEdit', 'formatDate',
                                                          function(rc, sc, http, log, periodEdit, formatDate)  {
		sc.selectedPeriodType = function() {
			return periodEdit.getSelectedPeriodType();
		}
		
		sc.selectedDegree = function() {
			return periodEdit.getSelectedDegree();
		}
		
		sc.selectedYear = function() {
			return periodEdit.getSelectedYear();
		}
		
		sc.remove = function() {
			periodEdit.remove();
		}
}]);