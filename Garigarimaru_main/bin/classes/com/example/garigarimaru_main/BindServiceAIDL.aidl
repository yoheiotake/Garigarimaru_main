package com.example.garigarimaru_main;

import com.example.garigarimaru_main.BindActivityAIDL;

interface BindServiceAIDL {
	void registerCallback(BindActivityAIDL callback);
	void unregisterCallback(BindActivityAIDL callback);
}
