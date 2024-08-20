requests = ['get_block -81,64,-17', 'stop']

import socket

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
try: client.connect(('127.0.0.1', int(2323)))
except ConnectionRefusedError: print('Server is offline')
print('Connection established')

try:
    while True:
        request = input('> ')
        client.sendall(chr(len(request)).encode('utf-16-be'))
        client.sendall(request.encode('utf-16-be'))

        length = int.from_bytes(client.recv(2))*2

        msg = client.recv(length)
        print(f'Response: {msg.decode("utf-16-be")}')
except ConnectionAbortedError:
    print('Connection aborted')