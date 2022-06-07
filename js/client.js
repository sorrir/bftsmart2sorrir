var net = require('net');

var client = new net.Socket();
client.connect(9999, '127.0.0.1', function() {
	console.log('Connected');
	let extendedPayload = JSON.stringify({text: 'Hello Server ;)'});
	// Custom protocol over Tcp:
    // 1. Write 32-bit Integer: total message length
    // 2. Write 32-bit Integer: length of message payload
    // 3. Write Bytes of JSON-parsed (UTF-8) message
    
    const payload = Buffer.from(extendedPayload);
    const toWrite = Buffer.alloc(4+4+payload.byteLength);
    toWrite.writeInt32BE(4 + payload.byteLength, 0);
    toWrite.writeInt32BE(payload.byteLength, 4);
    toWrite.write(extendedPayload, 8, 'utf8');
    
    console.log("payload bytes " + payload.byteLength);
  
    console.log("toWrite " + toWrite.byteLength);
    client.write(toWrite);
	
});

client.on('data', function(data) {
	console.log('Received: ' + data);
	client.destroy(); // kill client after server's response
});

client.on('close', function() {
	console.log('Connection closed');
});
