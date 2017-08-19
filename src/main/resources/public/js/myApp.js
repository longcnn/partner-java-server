var app = angular.module('myApp', []);
app.directive('onFinishRenderFilters', function ($timeout) {
    return {
        restrict: 'A',
        link: function (scope) {
            if (scope.$last === true) {
                $timeout(function () {
                    scope.$emit('ngRepeatFinished');
                });
            }
        }
    };
});
app.controller('site', function ($scope, $http) {
    $scope.url = "";
    $scope.$on('ngRepeatFinished', function () {
        $("table tr:last").css("border-bottom", "1px solid #ddd");
        var up = $("table tbody tr:first td:first");
        if (up.find(" span:last").text() === "../") {
            up.css("padding-top", "12px").css("padding-bottom", "12px");
        }
    });
    var listFile = function () {
        $http({
            method: 'GET',
            url: '../file/list?path=' + $scope.url
        }).then(function successCallback(response) {
            $scope.files = response.data;
        }, function errorCallback(response) {
            console.log(response);
        });
    };
    listFile();
    $("#fileUpload").fileinput({
        language: 'zh',
        uploadUrl: "../file/upload",
        showPreview: false,
        browseClass: "btn btn-default",
        enctype: 'multipart/form-data',
        uploadExtraData: function () {
            return {url: $scope.url};
        }
    }).on("fileuploaded", function (event, data) {
        if (data.response.code === 1) {
            listFile();
        } else {
            console.log(data.response);
        }
    });
    $scope.enterEvent = function (e) {
        var keycode = window.event ? e.keyCode : e.which;
        if (keycode === 13) {
            listFile();
        }
    };
    $scope.intoDirectory = function (file) {
        if (file.name === "../") {
            var url = $scope.url.substr(0, $scope.url.length - 1);
            var lastIndex = url.lastIndexOf("/");
            $scope.url = lastIndex <= 0 ? "" : url.substring(0, lastIndex + 1);
        } else {
            $scope.url = $scope.url + file.name;
        }
        listFile();
    };
    $scope.download = function (fileName) {
        window.location.href = "../file/download?fileName=" + $scope.url + fileName;
    };
    $scope.preview = function (fileName) {
        window.open("../static/" + $scope.url + fileName);
    };
});