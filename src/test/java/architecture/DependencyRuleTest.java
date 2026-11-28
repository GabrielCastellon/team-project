package architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import javax.swing.JPanel;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Enforces inward dependencies as teammates add real feature classes. */
class DependencyRuleTest {
    @Test
    void layersDoNotDependOnOuterImplementations() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("app", "entity", "use_case", "interface_adapter", "data_access", "view");
        noClasses().that().resideInAPackage("entity..")
                .should().dependOnClassesThat().resideOutsideOfPackages("entity..", "java..")
                .check(classes);
        noClasses().that().resideInAPackage("use_case..")
                .should().dependOnClassesThat()
                .resideOutsideOfPackages("use_case..", "entity..", "java..")
                .check(classes);
        noClasses().that().resideInAPackage("interface_adapter..")
                .should().dependOnClassesThat().resideInAnyPackage("app..", "view..", "data_access..", "javax.swing..")
                .check(classes);
        noClasses().that().resideInAPackage("data_access..")
                .should().dependOnClassesThat().resideInAnyPackage("app..", "view..", "interface_adapter..")
                .check(classes);
        noClasses().that().resideInAPackage("view..")
                .should().dependOnClassesThat().resideInAnyPackage("app..", "data_access..", "use_case..")
                .check(classes);
        noClasses().that().resideInAPackage("interface_adapter..")
                .should().dependOnClassesThat(resideInAPackage("use_case..").and(nameEndingWith("Interactor")))
                .check(classes);
        classes().that().areAssignableTo(JPanel.class)
                .should().resideInAPackage("view..")
                .check(classes);
    }
}
