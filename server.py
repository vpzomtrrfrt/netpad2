port = 4242

import socket, threading, uinput, traceback

def send(c, msg):
	print(msg)
	c.send(msg.encode('utf-8'))

def listenthread(c):
	f = c.makefile()
	print("Waiting for handshake...")
	gl = f.readline()
	gs = gl.split(',')
	dev = None
	if len(gs) == 2:
		try:
			btns = []
			for i in range(0, int(gs[0])):
				btns.append((3, i))
			for i in range(0, int(gs[1])):
				btns.append((1, 256+i))
			dev = uinput.Device(btns)
			send(c, 'OK')
		except:
			traceback.print_exc()
			send(c, 'Unable to create device')
			c.close()
	else:
		send(c, 'Wrong number of arguments')
		c.close()
	if dev != None:
		while True:
			l = f.readline()
			print(l)
			if not l:
				c.close()
			else:
				ch = l[0]
				ind = int(l[1:l.index(":")])
				v = float(l[l.index(":")+1:])
				if ch == "A":
					print((3, ind, int(v*32767)))
					dev.emit((3, ind), int(v*32767))
				elif ch == "B":
					print((1, ind+256, int(v)))
					dev.emit((1, ind+256), int(v))
				else:
					send(c, 'Invalid input type')
s = socket.socket()
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(('', port))
s.listen(5)
while True:
	c, a = s.accept()
	print(str(a)+" has conected")
	t = threading.Thread(target=listenthread, args=(c,))
	t.daemon = True
	t.start()
