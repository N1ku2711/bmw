let connection = new WebSocket('ws://127.0.0.1:7777');

function onSendBtnClick(){
	connection.send('Am apasat butonul click')
}
connection.onopen = function()

{
	console.log('Connected!');
	connection.send('Buna , Server!');
};


connection.onerror = function(error)
{
	console.log('WebSocket Error: '+ error);
};


connection.onmessage = function(e)
{
	console.log(e.data);
};