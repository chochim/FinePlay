package common.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import common.utils.Binaries;

class IntegrityValuesCreator {

	private static final String ALGORITHM = "SHA-384";

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {

		final SortedMap<String, String> constMap = createValues();

		writeJavaFile(constMap);
	}

	private static SortedMap<String, String> createValues() throws IOException {

		final SortedMap<String, String> constMap = new TreeMap<>();

		constMap.put("BOOTSTRAP_STYLE", createValue(Paths.get(".", "target", "web", "public", "main", "lib", "bootstrap", "dist", "css", "bootstrap.min.css")));
		constMap.put("BOOTSTRAP_STYLE_PRETTY", createValue(Paths.get(".", "public", "themes", "pretty", "bootstrap.min.css")));
		constMap.put("BOOTSTRAP_STYLE_JAPAN", createValue(Paths.get(".", "public", "themes", "japan", "bootstrap.min.css")));
		constMap.put("BOOTSTRAP_SCRIPT", createValue(Paths.get(".", "target", "web", "public", "main", "lib", "bootstrap", "dist", "js", "bootstrap.min.js")));

		constMap.put("JQUERY_SCRIPT", createValue(Paths.get(".", "target", "web", "public", "main", "lib", "jquery", "dist", "jquery.min.js")));

		constMap.put("JQUERY_UI_STYLE", createValue(Paths.get(".", "target", "web", "public", "main", "lib", "jquery-ui", "jquery-ui.min.css")));
		constMap.put("JQUERY_UI_SCRIPT", createValue(Paths.get(".", "target", "web", "public", "main", "lib", "jquery-ui", "jquery-ui.min.js")));

		return Collections.unmodifiableSortedMap(constMap);
	}

	@SuppressWarnings("null")
	private static String createValue(final Path subresourcePath) throws IOException {

		if (!Files.exists(subresourcePath)) {

			throw new RuntimeException("Not exists subresource. : " + subresourcePath);
		}

		System.out.println("Processing... " + subresourcePath);
		return toHashedValue(Files.readAllBytes(subresourcePath));
	}

	private static void writeJavaFile(final SortedMap<String, String> constMap) throws IOException {

		final Builder builder = TypeSpec.classBuilder("IntegrityValues")//
				.addJavadoc("Generated by JavaPoet.\n") //
				.addJavadoc("@see <a href=\"https://github.com/square/javapoet\">https://github.com/square/javapoet</a>\n") //
				.addModifiers(Modifier.PUBLIC);

		final MethodSpec constructorSpec = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
		builder.addMethod(constructorSpec);

		for (final Entry<String, String> entry : constMap.entrySet()) {

			final FieldSpec fieldSpec = FieldSpec.builder(String.class, entry.getKey(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)//
					.initializer("$S", entry.getValue())//
					.build();
			builder.addField(fieldSpec);
		}

		final TypeSpec typeSpec = builder.build();

		final JavaFile javaFile = JavaFile.builder("common.system", typeSpec).build();

		final Path constantsPath = Paths.get(".", "app");
		javaFile.writeTo(constantsPath);
	}

	@SuppressWarnings("null")
	@Nonnull
	private static String toHashedValue(@Nonnull final byte[] subresourceBytes) {

		Objects.requireNonNull(subresourceBytes);

		return Base64.getMimeEncoder().encodeToString(Binaries.toHash(ALGORITHM, subresourceBytes));
	}
}