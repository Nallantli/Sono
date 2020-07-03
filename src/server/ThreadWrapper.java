package server;

import main.SonoWrapper;
import main.sono.Datum;

public class ThreadWrapper extends Thread {
	private SonoWrapper wrapper;
	private String code;
	private Datum ret;

	public ThreadWrapper(SonoWrapper wrapper, String code) {
		this.wrapper = wrapper;
		this.code = code;
	}

	@Override
	public void run() {
		System.out.println("RUNNING SERVER CODE THREAD\t" + Thread.currentThread());
		ret = wrapper.run(code);
	}

	public Datum getReturn() {
		return ret;
	}
}