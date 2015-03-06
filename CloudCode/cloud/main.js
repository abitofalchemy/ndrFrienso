
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("sendTrackEventStartNotification", function(request, response) {
  var senderUser = request.user;
  var recipientUserId = request.params.recipientId;
  var message = request.params.message;
  var msgTitle = request.params.title;

  var query = new Parse.Query("CoreFriendRequest");
  query.equalTo("sender",senderUser);
  query.include("recipient");
  query.find({
    success: function(results) {
         console.log("result len = " + results.length);
         channelArray = []
         for (var i = 0; i< results.length;i++) {
             var object = results[i];
             var user = object.get("recipient");
             channelArray[channelArray.length] = "Ph"+user.get("phoneNumber");
         }
         console.log("channel array = " + channelArray);
         Parse.Push.send({
             channels: channelArray,
             data: {
                 alert:message,
                 title:msgTitle
             }
      }).then(function() {
      response.success("Pusyyh was sent successfully.")
      }, function(error) {
      response.error("Push failed to send with error: " + error.message);
     });
    },
     error: function(error){   
        response.error("unable to query friends. this is the error" + error.message);
     }
  });
});
