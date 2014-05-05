/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.lesscss;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Compiles LESS input into CSS, using less.js on Mozilla Rhino.
 * 
 * @author Emanuel Rabina
 */
public class LessCSSCompiler {

	private static final String ENV_RHINO_JS = "nz/net/ultraq/lesscss/env.rhino-1.2.13.js";
	private static final String LESS_JS      = "nz/net/ultraq/lesscss/less-1.7.0.js";

	/* (function() {
	 * 	 var result = null;
	 *   var parser = new less.Parser({
	 *     filename: '{0}'
	 *   });
	 * 
	 *   parser.parse('{1}', function(err, tree) {
	 *     result = err ? err : tree.toCSS();
	 *   });
	 *   return result;
	 * })();
	 */
	private static final String compilejs = "(function() { var result = null; var parser = new less.Parser({ filename: '{0}' }); parser.parse('{1}', function(err, tree) { result = err ? err : tree.toCSS(); }); return result; })();";

	private final Scriptable scope;

	/**
	 * Create a new LESS compiler.
	 * 
	 * @throws LessCSSException If there was a problem initializing the compiler.
	 */
	public LessCSSCompiler() throws LessCSSException {

		try {
			Context context = Context.enter();
			context.setLanguageVersion(Context.VERSION_1_8);
			context.setOptimizationLevel(-1);
			Global global = new Global();
			global.init(context);
			scope = context.initStandardObjects(global);

			context.evaluateReader(scope, new BufferedReader(new InputStreamReader(
					LessCSSCompiler.class.getClassLoader().getResource(ENV_RHINO_JS).openStream())),
					"env.rhino.js", 1, null);
			context.evaluateReader(scope, new BufferedReader(new InputStreamReader(
					LessCSSCompiler.class.getClassLoader().getResource(LESS_JS).openStream())),
					"less.js", 1, null);
		}
		catch (IOException ex) {
			throw new LessCSSException("Unable to initialize LESS compiler.", ex);
		}
		finally {
			Context.exit();
		}
	}

	/**
	 * Compile the LESS input into CSS.
	 * 
	 * @param input	 The LESS file to compile.
	 * @param output The CSS file to write the compiled result to.
	 * @throws LessCSSException
	 */
	public void compile(File input, File output) throws LessCSSException {

		Scanner scanner = null;
		String result;
		try {
			scanner = new Scanner(input);
			scanner.useDelimiter("\\A");
			String inputstring = scanner.hasNext() ? scanner.next() : "";
			result = compile(input.getName(), inputstring);
		}
		catch (FileNotFoundException ex) {
			throw new LessCSSException("Input file " + input.getName() + " doesn't exist", ex);
		}
		finally {
			if (scanner != null) {
				scanner.close();
			}
		}

		try {
			if (!output.exists()) {
				output.createNewFile();
			}
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(output));
				writer.write(result);
			}
			finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
		catch (IOException ex) {
			throw new LessCSSException("Unable to write compiled CSS", ex);
		}
	}

	/**
	 * Compile the LESS input into CSS.
	 * 
	 * @param input The LESS input to compile.
	 * @return Compiled LESS input.
	 * @throws LessCSSException
	 */
	public String compile(String input) {

		return compile("(inline input)", input);
	}

	/**
	 * Compile the LESS input into CSS.
	 * 
	 * @param filename Name of the LESS file being compiled.
	 * @param input	   The LESS input to compile.
	 * @return Compiled LESS input.
	 * @throws LessCSSException
	 */
	protected String compile(String filename, String input) throws LessCSSException {

		try {
			Context context = Context.enter();
			context.setLanguageVersion(Context.VERSION_1_8);

			// Process the LESS input
			String processless = compilejs.replace("{0}", filename)
					.replace("{1}", input.replace("'", "\\'").replaceAll("\\s", " "));
			return context.evaluateString(scope, processless, "process-less.js", 1, null).toString();
		}
		catch (JavaScriptException ex) {
			throw new LessCSSException("Unable to process LESS input from " + filename, ex);
		}
		finally {
			Context.exit();
		}
	}
}
