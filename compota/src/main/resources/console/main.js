//    $(document).ready(function(){
//
//    if (!window.console) console = {};
//    console.log = console.log || function(){};
//    console.warn = console.warn || function(){};
//    console.error = console.error || function(){};
//    console.info = console.info || function(){};
//
//    $('#refresh').click(function(){
//        console.info("refresh:");
//        $('#workerInstances').load("/nispero/$nispero$/workerInstances", function() {
//        $('.terminate').click(function(event){
//        console.info("terminate: " + event.target.id);
//        $('.terminateFinal').attr("id", event.target.id)
//        $('.terminateInstanceBody').html("<p>Do you want to terminate instance " + event.target.id + "?</p>")
//        $('#terminateInstanceModal').modal('show')
//        });
//        });
//    });
//
//    $('.terminate').click(function(event){
//        console.info("terminate: " + event.target.id);
//        $('.terminateFinal').attr("id", event.target.id)
//        $('.terminateInstanceBody').html("<p>Do you want to terminate instance " + event.target.id + "?</p>")
//        $('#terminateInstanceModal').modal('show')
//    });
//
//   // $("#workerInstances .terminate").on( "click", function(event) {
//   //     console.info("terminate: " + event.target.id);
//   //     $('.terminateFinal').attr("id", event.target.id)
//   //     $('.terminateInstanceBody').html("<p>Do you want to terminate instance " + event.target.id + "?</p>")
//   //     $('#terminateInstanceModal').modal('show')
//   // });
//
//    $('.terminateFinal').click(function(event){
//        console.info("final terminate: " + event.target.id);
//        $('.terminateInstanceBody').load("/terminate/" + event.target.id)
//    });
//
//    $('.showMessages').click(function(event){
//      //  console.info("show messages:" + event.target.nodeName)
//       // console.info(event.target.data('queue'))
//       var q = $(event.target).data('queue')
//        console.info("show messages: queue:" + q);
//
//        $('#messages-' + q).load('/queue/' + q + '/messages/', function() {
//          $('.loadMore[data-queue="' + q + '"]').click(function(event){
//            var loadMore = event.target
//            var q2 = event.target.attr('data-queue')
//            var lastKey = event.target.attr('data-lastKey')
//            console.info("load more: queue=" + q2 + " lastKey=" + lastKey);
//
//            var query = '/queue/' + q2 + '/messages'
//            if(lastKey) {
//                query += "/" + lastKey
//            }
//            $.get(query, function(data) {
//                 loadMore.replaceWith(data);
//            });
//          });
//        });
//    });
//
//    });