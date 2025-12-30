package me.lauriichan.snowframe.maven;

import static me.lauriichan.maven.sourcemod.api.SourceTransformerUtils.*;

import java.util.List;

import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;

import me.lauriichan.maven.sourcemod.api.ISourceTransformer;
import me.lauriichan.snowframe.signal.ISignalHandler;
import me.lauriichan.snowframe.signal.SignalContainer;
import me.lauriichan.snowframe.signal.SignalContext;
import me.lauriichan.snowframe.signal.SignalHandler;
import me.lauriichan.snowframe.signal.SignalReceiver;

public final class SignalHandlerTransformer implements ISourceTransformer {

    @Override
    public boolean canTransform(JavaSource<?> source) {
        if (!(source instanceof final JavaClassSource classSource)) {
            return false;
        }
        return !classSource.isAbstract() && !classSource.isRecord() && classSource.hasInterface(ISignalHandler.class);
    }

    @Override
    public void transform(JavaSource<?> source) {
        final JavaClassSource clazz = (JavaClassSource) source;

        StringBuilder containerBuilder = new StringBuilder("""
            @Override
            public SignalContainer newContainer() {
                return new SignalContainer(this, new SignalReceiver[] {
            """);
        int amount = 0;
        for (final MethodSource<JavaClassSource> method : clazz.getMethods()) {
            if (!method.hasAnnotation(SignalHandler.class)
                || !(method.getReturnType().isType(void.class) || method.getReturnType().isType(Void.class))) {
                continue;
            }
            List<ParameterSource<JavaClassSource>> params = method.getParameters();
            if (params.size() != 1) {
                continue;
            }
            Type<JavaClassSource> paramType = params.get(0).getType();
            if (!paramType.isType(SignalContext.class) || !paramType.isParameterized()) {
                continue;
            }
            Type<JavaClassSource> packetType = paramType.getTypeArguments().get(0);
            if (amount++ != 0) {
                containerBuilder.append(",");
            }
            containerBuilder.append("\n\t\tnew SignalReceiver<>(").append(packetType.getQualifiedName()).append(".class, this::")
                .append(method.getName()).append(", ")
                .append(Boolean.parseBoolean(method.getAnnotation(SignalHandler.class).getLiteralValue())).append(')');
        }
        if (amount == 0) {
            return;
        }

        removeMethod(clazz, "newContainer");

        importClass(clazz, SignalContainer.class);
        importClass(clazz, SignalReceiver.class);

        containerBuilder.append('\n').append("""
                });
            }
            """);
        clazz.addMethod(containerBuilder.toString());
        containerBuilder = null;
    }

}
