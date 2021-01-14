//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package plugin.android.assets;

import android.content.res.AssetManager;
import android.util.Log;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.ansca.corona.storage.FileServices;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.JavaModule;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.NamedJavaFunction;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Implements the Lua interface for a Corona plugin.
 * <p>
 * Only one instance of this class will be created by Corona for the lifetime of the application.
 * This instance will be re-used for every new Corona activity that gets created.
 */
@SuppressWarnings("WeakerAccess")
public class LuaLoader implements JavaFunction {
	private static final String TAG = "Corona_plugin_assets";
	/**
	 * Creates a new Lua interface to this plugin.
	 * <p>
	 * Note that a new LuaLoader instance will not be created for every CoronaActivity instance.
	 * That is, only one instance of this class will be created for the lifetime of the application process.
	 * This gives a plugin the option to do operations in the background while the CoronaActivity is destroyed.
	 */
	@SuppressWarnings("unused")
	public LuaLoader() {
		// Set up this plugin to listen for Corona runtime events to be received by methods
		// onLoaded(), onStarted(), onSuspended(), onResumed(), and onExiting().
		// CoronaEnvironment.addRuntimeListener(this);
	}

	/**
	 * Called when this plugin is being loaded via the Lua require() function.
	 * <p>
	 * Note that this method will be called every time a new CoronaActivity has been launched.
	 * This means that you'll need to re-initialize this plugin here.
	 * <p>
	 * Warning! This method is not called on the main UI thread.
	 * @param L Reference to the Lua state that the require() function was called from.
	 * @return Returns the number of values that the require() function will return.
	 *         <p>
	 *         Expected to return 1, the library that the require() function is loading.
	 */
	@Override
	public int invoke(LuaState L) {
		// Register this plugin into Lua with the following functions.
		NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
			new ReadWrapper(),
			new OpenWrapper(),
			new GetReaderWrapper(),
			new ExistsWrapper(),
			new GetFileServicesWrapper(),
			new DoesAssetFileExistWrapper(),
			new NamedJavaFunction() {
				@Override
				public String getName() {
					return "test";
				}

				@Override
				public int invoke(LuaState luaState) {
					FileServices fileServices = new FileServices(CoronaEnvironment.getApplicationContext());
					luaState.pushJavaObject(fileServices);
					return 1;
				}
			}
		};
		String libName = L.toString( 1 );
		L.register(libName, luaFunctions);

		// Returning 1 indicates that the Lua require() function will return the above Lua library.
		return 1;
	}

	/**
	 * The following Lua function has been called:  library.check( listener )
	 * <p>
	 * Warning! This method is not called on the main thread.
	 * @param L Reference to the Lua state that the Lua function was called from.
	 * @return Returns the number of values to be returned by the library.check() function.
	 */
	@SuppressWarnings({"WeakerAccess", "SameReturnValue"})
	public int read(LuaState L) {
		String content = null;

		int pathIndex = 1;
		String assetName = "";
		LuaType type = L.type(pathIndex);
		if (type == LuaType.STRING) {
			assetName = L.toString(pathIndex);
		} else {
			content = "Assets.read need string path, got " + type.displayText();
			Log.d(TAG, content);
			L.pushBoolean(false);
			L.pushString(content);
			return 2;
		}

		android.content.Context context = CoronaEnvironment.getApplicationContext();
		if (context == null) {
			content = "Assets.read failed, context is null.";
			Log.d(TAG, content);
			L.pushBoolean(false);
			L.pushString(content);
			return 2;
		}

		// AssetManager assetManager = context.getAssets();
		// try {
		// 	String[] filePathList = assetManager.list("");
		// 	Log.d(TAG, Arrays.toString(filePathList));
		// } catch (Exception ex) {
		// 	ex.printStackTrace();
		// }

		FileServices fileServices = new FileServices(context);
		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			Log.d(TAG, assetName);
			inputStream = fileServices.openFile(assetName);
			if (inputStream != null) {
				outputStream = new ByteArrayOutputStream();
				final int BUFFER_SIZE = 1024;
				byte[] byteBuffer = new byte[BUFFER_SIZE];
				int n;
				while ((n = inputStream.read(byteBuffer)) != -1) {
					outputStream.write(byteBuffer, 0, n);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			content = ex.toString() + "\n" + ex.getMessage();
		} finally {
			try {
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != outputStream) {
					outputStream.close();
				}
			} catch(Exception e) {
				e.printStackTrace();
				content = e.toString() + "\n" + e.getMessage();
			}
		}
		if (outputStream != null) {
			L.pushBoolean(true);
		    L.pushString(outputStream.toByteArray());
		} else {
			if (content == null) {
				content = "Assets.read buffer is null, There is no assets file named " + assetName;
			}
			Log.d(TAG, content);
			L.pushBoolean(false);
			L.pushString(content);
		}
		return 2;
	}

	@SuppressWarnings({"WeakerAccess", "SameReturnValue"})
	public int open (LuaState L) {
		boolean yes = false;
		int pathIndex = 1;
		String assetName = "";
		if (L.type(pathIndex) == LuaType.STRING) {
			assetName = L.toString(pathIndex);

			android.content.Context context = CoronaEnvironment.getApplicationContext();
			if (context != null) {
				FileServices fileServices = new FileServices(context);
				InputStream inputStream = fileServices.openFile(assetName);
				if (inputStream == null) {
					L.pushNil();
				} else {
					L.pushJavaObject(inputStream);
				}
				yes = true;
			}
		}

		if (!yes) {
			L.pushNil();
		}
		return 1;
	}

	@SuppressWarnings({"WeakerAccess", "SameReturnValue"})
	public int getReader (LuaState L) {
		boolean yes = false;
		int pathIndex = 1;
		String assetName = "";
		if (L.type(pathIndex) == LuaType.STRING) {
			assetName = L.toString(pathIndex);

			android.content.Context context = CoronaEnvironment.getApplicationContext();
			if (context != null) {
				FileServices fileServices = new FileServices(context);
				InputStream inputStream = fileServices.openFile(assetName);
				if (inputStream == null) {
					L.pushNil();
				} else {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					L.pushJavaObject(reader);
				}
				yes = true;
			}
		}

		if (!yes) {
			L.pushNil();
		}
		return 1;
	}

	@SuppressWarnings({"WeakerAccess", "SameReturnValue"})
	public int exists (LuaState L) {
		boolean yes = false;
		int pathIndex = 1;
		String assetName = "";
		if (L.type(pathIndex) == LuaType.STRING) {
			assetName = L.toString(pathIndex);

			android.content.Context context = CoronaEnvironment.getApplicationContext();
			if (context != null) {
				FileServices fileServices = new FileServices(context);
				InputStream inputStream = fileServices.openFile(assetName);
				yes = inputStream != null;
				try {
				    if (yes)
						inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		L.pushBoolean(yes);
		return 1;
	}

	@SuppressWarnings({"WeakerAccess", "SameReturnValue"})
	public int doesAssetFileExist (LuaState L) {
	    boolean yes = false;
		int pathIndex = 1;
		String assetName = "";
		if (L.type(pathIndex) == LuaType.STRING) {
			assetName = L.toString(pathIndex);

			android.content.Context context = CoronaEnvironment.getApplicationContext();
			if (context != null) {
				FileServices fileServices = new FileServices(context);
				yes = fileServices.doesAssetFileExist(assetName);
			}
		}

		L.pushBoolean(yes);
		return 1;
	}

	@SuppressWarnings({"WeakerAccess", "SameReturnValue"})
	public int getFileServices (LuaState L) {
		boolean yes = false;
		int pathIndex = 1;
		String assetName = "";
		if (L.type(pathIndex) == LuaType.STRING) {
			assetName = L.toString(pathIndex);

			android.content.Context context = CoronaEnvironment.getApplicationContext();
			if (context != null) {
				FileServices fileServices = new FileServices(context);
				L.pushJavaObject(fileServices);
				yes = true;
			}
		}

		if (!yes) {
			L.pushNil();
		}
		return 1;
	}

	/** Implements the library.read() Lua function. */
	@SuppressWarnings("unused")
	private class ReadWrapper implements NamedJavaFunction {
		/**
		 * Gets the name of the Lua function as it would appear in the Lua script.
		 * @return Returns the name of the custom Lua function.
		 */
		@Override
		public String getName() {
			return "read";
		}
		
		/**
		 * This method is called when the Lua function is called.
		 * <p>
		 * Warning! This method is not called on the main UI thread.
		 * @param L Reference to the Lua state.
		 *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
		 * @return Returns the number of values to be returned by the Lua function.
		 */
		@Override
		public int invoke(LuaState L) {
			return read(L);
		}
	}

	private class OpenWrapper implements NamedJavaFunction {
		@Override
		public String getName() {
			return "open";
		}
		@Override
		public int invoke(LuaState L) {
			return open(L);
		}
	}

	private class ExistsWrapper implements NamedJavaFunction {
		@Override
		public String getName() {
			return "exists";
		}
		@Override
		public int invoke(LuaState L) {
			return exists(L);
		}
	}

	private class GetFileServicesWrapper implements NamedJavaFunction {
		@Override
		public String getName() {
			return "getFileServices";
		}
		@Override
		public int invoke(LuaState L) {
			return getFileServices(L);
		}
	}

	private class GetReaderWrapper implements NamedJavaFunction {
		@Override
		public String getName() {
			return "getReader";
		}
		@Override
		public int invoke(LuaState L) {
			return getReader(L);
		}
	}

	@SuppressWarnings("unused")
	private class DoesAssetFileExistWrapper implements NamedJavaFunction {
		/**
		 * Gets the name of the Lua function as it would appear in the Lua script.
		 * @return Returns the name of the custom Lua function.
		 */
		@Override
		public String getName() {
			return "doesAssetFileExist";
		}

		/**
		 * This method is called when the Lua function is called.
		 * <p>
		 * Warning! This method is not called on the main UI thread.
		 * @param L Reference to the Lua state.
		 *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
		 * @return Returns the number of values to be returned by the Lua function.
		 */
		@Override
		public int invoke(LuaState L) {
			return doesAssetFileExist(L);
		}
	}
}
