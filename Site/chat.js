let myStorage = sessionStorage;

let token = myStorage.getItem("token");

if (token == null) {
	window.open ('login.html', '_self', false);
}

let connection = new WebSocket('ws://127.0.0.1:7777/chat');

connection.onopen = function(){
	let token_auth = {
		type: "token_auth",
		token: token
	}
	let json = JSON.stringify(token_auth);
	connection.send(json);
};

connection.onmessage = function(e){
	let msg = JSON.parse(e.data);
	if (msg['type'] == ('token_auth')) {
		alertify.success('Hello, ' + msg['name'] + '!');
	} else if (msg['type'] == ('sending_result')) {
		if (msg['result'] == 'success') {
			activateMessage(msg['label']);
		} else {
			deleteMessage(msg['label']);
		}
	} else if (msg['type'] == 'chat_message') {
		if (msg['isMine'] == 'true') {
			createMyMessage(msg['text'], new Date().getTime(), '');
		} else {
			createMessage(msg['text'], msg['sender']);
		}
	} else if (msg['type'] == 'chat_history') {
		let array = msg['messages'];
		for (let i = 0; i < array.length; i++) {
			if (array[i]['isMine'] == 'true') {
				createMyMessage(array[i]['text'], new Date().getTime(), '');
			} else {
				createMessage(array[i]['sender'], array[i]['text']);
			}
		}
	}
};

function logout() {
	myStorage.clear();
	window.open ('index.html', '_self', false);
}

function sendMessage() {
	let msg = $("#inputMessageText").val(); //Получаем значение с поля ввода
	if (msg == "") return;
	let label = new Date().getTime();

	let message = {
		type: "chat_message",
		token: token,
		text: msg,
		label: label
	}

	let json = JSON.stringify(message);
	connection.send(json);
	$("#inputMessageText").val("");
	createMyMessage(msg, label, 'disabled');
}

function createMyMessage(text, label, disabled) {
 	let messageContainerStr = '<li class = "message own ' + disabled + '" id = "id' + label + '"> </li>';
	let messageContainer = $(messageContainerStr);
	let messageTextElementStr = '<p class = "message-text">' + text + '</p>';
	let messageTextElement = $(messageTextElementStr);
	messageContainer.append(messageTextElement);

	$("#messages-list").append(messageContainer);
	$(".messages-container").scrollTop(99999);
}

function createMessage(text, name) {
	let avatarStr = '<div class="message-avatar"><p>' + name.charAt(0) + '</p></div>'
	let avatar = $(avatarStr);
	let messageContainerStr = '<li class = "message"> </li>';
	let messageContainer = $(messageContainerStr);
	let messageTextElementStr = '<p class = "message-text">' + text + '</p>';
	let messageTextElement = $(messageTextElementStr);

	messageContainer.append(avatar);
	messageContainer.append(messageTextElement);

	$("#messages-list").append(messageContainer);
	$(".messages-container").scrollTop(99999);
}

function activateMessage(label) {
	let id = "#id" + label;
	$(id).removeClass("disabled");
}

function deleteMessage(label) {
	let id = "#id" + label;
	$(id).remove();
}


$(document).ready(function() {
  $('#inputMessageText').keydown(function(e) {
    if(e.keyCode === 13) {
      sendMessage();
    }
  });
});