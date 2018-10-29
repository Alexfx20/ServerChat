 $(function(){
            $('li.active').css('display', 'table-cell');
            $('#tabs-0').show();
        });

        function getDataAndConnect(){
            if(!isOnChat){
                isOnChat = true;
        maxConnextions = document.getElementById('connections').value;
       
        name = $('input[name=login]').val();//получаем значение поля input которое выбрано.
        if(name ==='') return;
        status = $('input[name=status]:checked').val();//получаем значение поля имя которое выбрано.
        creatTabs(maxConnextions);}
        
    }
        function creatTabs(maxConnextions){ //визуализирует табы
            var max = document.getElementById("connections").max;
            
                var lielements = document.getElementsByTagName('li');//ссылка на li эелменты эта ссылка динамична и изменяется по ходу изменения li
                var ulElement = document.getElementById("items");
            if(lielements.length!=maxConnextions){
               
                if(lielements.length!=1){                       //т.к. кол-во элментов при удалении уменьшается то и максимальный инедкс li элемента тоже уменьшается
                    deleteAllTabsExceptTheFirstOne();
                } 

                var liSample = ulElement.children[0];//получаем первый элемент li
               // console.log(liSample);                                      
                var tabsSample = document.getElementById("tabs-0");         //получаем элемент таб-0
                //console.log(tabsSample);
                for (var i = 1; i < maxConnextions; i++) {                  
                    var cloneEl = tabsSample.cloneNode(true);                 //клонируем таб
                    cloneEl.id = "tabs-"+i;                                   //меняем id
                    var inp = cloneEl.getElementsByTagName('input');
                    inp[0].id = '$'+i;
                    var func1="wsSendMessage('"+inp[0].id+"')";
                    var func2="leaveChat('"+inp[0].id+"')";    //создаем имя функции к-рая выполняется при нажатии на кнопку
                    inp[1].setAttribute("onclick", func1 );      //задаем событие кнопке эта функция сработала в отличие от JQ
                    inp[2].setAttribute("onclick", func2 );
                    var textA = cloneEl.getElementsByTagName('textarea'); //меняем id для поля ввода
                    textA[0].id="messageArrea$"+i;
                   //console.log(cloneEl);
                    var tabs = document.getElementById('t-cont');
                    //console.log(tabs);
                    $(cloneEl).css('display', 'none');
                    tabs.appendChild(cloneEl);              //не сработала в JQ
                   
                    var li = $("<li id = li-"+i+"><a href= '#tabs-"+i+"'>Chat "+(i+1)+"</a></li>");
                    //console.log(li);
                    li.appendTo($('#items'));
                }
            
            }    
                for (var i = 0; i < lielements.length; i++) {
                    $(lielements[i]).css('display', 'table-cell');
                }
                buttonEventListener();
                connect();
        }

        function deleteAllTabsExceptTheFirstOne(){
            var lielements = document.getElementsByTagName('li');//ссылка на li эелменты эта ссылка динамична и изменяется по ходу изменения li
                var fixedLi = lielements.length;
                var ulElement = document.getElementById("items");
                var tabsContainer = document.getElementById("t-cont");
                console.log("li="+lielements.length);
                if(lielements.length!=1){                       //т.к. кол-во элментов при удалении уменьшается то и максимальный инедкс li элемента тоже уменьшается
                    for (var i = 1; i < fixedLi; i++) {         // удаление проиходит со сдвигом элементов в лево.
                        ulElement.removeChild(lielements[1]);   // поэтому лучше удалять всегда 1 элемент если необходимо удалить все элементы
                                                                // для итерации лучше предварительно создать константу.
                        tabsContainer.removeChild(tabsContainer.children[1]);
                    }
                }
                lielements[0].className = 'active';             //возвращаем видимость первого элемента
                $('li.active').css('background', '#f3f3f3');    //подсвечиваем его и ссылку на него
                 $('#tabs-0').show();
        }




        function buttonEventListener(){      //регистрация слушателей
            $('#items li').click(function(){
                if($(this).attr('class') == 'active'){
                    return false;
                }

                var link = $(this).children().attr('href'); //неактивная вкладка к открытию
                var prevActive = $('li.active').children().attr('href');
                $('li.active').removeClass('active');
                if($(this).attr('class') =='recieved'){
                    $(this).removeClass('recieved');
                }
                $(this).addClass('active');
                //изменение состояния текста
                $(prevActive).fadeOut(250, function(){
                    $(link).fadeIn();
                
                //изменение цвета вкладок
                var lielements = document.getElementsByTagName('li');
                for (var i = 0; i < lielements.length; i++) {
                    console.log($(lielements[i]).attr('class'));
                    if($(lielements[i]).attr('class')!='recieved'){
                        $(lielements[i]).css('background', '#bbb');
                        console.log(i);
                    }
                }
                //$('#items li').css('background', '#bbb');
                $('li.active').css('background', '#f3f3f3');
                });
                return false;
            });
        }

        var webSocket;
        var message = "hello";
        var maxConnextions;
        var name;
        var status;
        var isOnChat = false;
	var isReceivedHelloMessage = false;//NEW INSERTION

        
        function connect(){
            webSocket = new WebSocket('ws://' + window.location.host + '/server-1.0-SNAPSHOT/ru/secondchat/web');
            webSocket.onopen = function(message){ wsOpen(message);};
            webSocket.onmessage = function(message){ wsGetMessage(message);};
            webSocket.onclose = function(message){ wsClose(message);};
            webSocket.onerror = function(message){ wsError(message);};

        }


        function wsOpen(message){
            var messageArea = document.getElementById('messageArrea$0');
            messageArea.value += "Connecting... \n";
        /*document.onkeydown = function(event) {
                    if (event.keyCode == 13) {
                        wsSendMessage();
                    }
            if (event.keyCode == 27){Disconnect();}
                };*/
            //register();//NEW INSERTION
        }

        function register(){
            if(maxConnextions==1){
                webSocket.send("/register "+name+" "+status+"$1");
                console.log("vasia"+name);
            }
            else webSocket.send("/register "+name+" "+status+"$"+maxConnextions);
           
        }

        function wsSendMessage(id){
        var mes =document.getElementById(id);
        if (mes !=''){
            //console.log(id);
            if(maxConnextions!=1)
            webSocket.send(mes.value+id);//мульти
            else webSocket.send(mes.value);//и сингл пользователи
            mes.value = "";}
        }

        function wsCloseConnection(){
            webSocket.close();
        }

        function wsGetMessage(message){
            console.log(message.data);
           var invokenTab = processMessage(message);
           console.log(invokenTab);
           notifyAboutMessage(invokenTab);
		
		if(!isReceivedHelloMessage&&isOnChat){//NEW INSERTION
			isReceivedHelloMessage = true;
			register();
			}
		
            
        }

        function notifyAboutMessage(mesArea){
            console.log(mesArea.id);
            var mes = mesArea.id.split("$");
            var id = "li-"+mes[1];
            var li = document.getElementById(id);
            if($(li).attr('class') != 'active'){
                  $(li).css('background', '#ADFF2F');
                  $(li).addClass('recieved'); 
                }

        }

        function processMessage(message){           //метод парсит входящее сообщение определяет id в сообщении, выбирает и отправляет сообщение на нужный таб
            var mes = message.data.split('$');
            console.log(mes[0]);
            var messageArea;
            if(mes.length<2||maxConnextions==1){
            messageArea = document.getElementById('messageArrea$0');
            messageArea.value += message.data + "\n";

        }
        else {var id = 'messageArrea$'+mes[mes.length-1];
            var messageArea = document.getElementById(id); 
            if(messageArea)
            messageArea.value +=mes[0] + "\n";
            else {
                messageArea = document.getElementById('messageArrea$0');
                messageArea.value += message.data + "\n";
            }
        }
            console.log(messageArea);
        messageArea.scrollTop = messageArea.scrollHeight;
            return messageArea;
        }

        function wsClose(message){
           var messageArea = document.getElementsByClassName('mesArea'); 
            for (var i = 0; i < messageArea.length; i++) {
                messageArea[i].value += "Connection closed \n";
            }
            
        message.onkeydown = null;
        }
 
        function wserror(message){
            var messageArea = document.getElementsByClassName('mesArea'); 
            for (var i = 0; i < messageArea.length; i++) {
                messageArea[i].value += "Error occured \n";
            }
        }

    function leaveChat(id){
        
        webSocket.send("/leave"+id)
        document.getElementById(id).value = "";
    }

    function Disconnect(){
        webSocket.send("/exit")
        var inpField = document.getElementsByClassName('inpField'); 
            for (var i = 0; i < inpField.length; i++) {
                inpField[i].value += "";
            }
            isOnChat = false;
		isReceivedHelloMessage = false;
        //message.value = "";
    }



