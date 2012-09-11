package org.primeframework.gradle.plugins

import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The database plugin is used for creating database
 *
 * @author James Humphrey
 */
class TomcatPlugin implements Plugin<Project> {

  def void apply(Project project) {

    project.extensions.add("tomcatConfig", new TomcatPluginConfiguration())

    project.task("tomcat", dependsOn: ["war"]) << {

      println "Setting up project to run in tomcat..."

      def catalinaHome = System.getenv("CATALINA_HOME")

      if (catalinaHome == null) {
        throw new GradleException("You must have an environment variable called CATALINA_HOME that points to your tomcat installation directory")
      }

      project.mkdir("${project.buildDir}/tomcat/logs")
      project.mkdir("${project.buildDir}/tomcat/temp")
      project.mkdir("${project.buildDir}/tomcat/work")
      project.mkdir("${project.buildDir}/tomcat/bin")

      String fileContents = this.class.classLoader.getResource("tomcat-files/bin/tomcat.sh").getText("UTF-8")
      fileContents = fileContents.
        replaceAll("@WEBAPP_ROOT@", project.tomcatConfig.webappRoot).
        replaceAll("@MAX_MEMORY@", project.tomcatConfig.maxMemory)

      FileUtils.writeStringToFile(new File("${project.buildDir}/tomcat/bin/tomcat.sh"), fileContents, "UTF-8")

      project.copy {
        from("${catalinaHome}/conf") {
          include "*"
        }
        into "${project.buildDir}/tomcat/conf"
      }

      ant.chmod(file: "${project.buildDir}/tomcat/bin/tomcat.sh", perm: "u+x")

      // copy the context.xml
      //
      // This is a workaround for Tomcat sucking. Tomcat can"t have a context.xml in the root of the
      // conf dir because it then assumes that context-path is the directory name that the web app is
      // running from. In our cases this would then always be /web, which sucks. So, instead I fake
      // out Tomcat to think that there is only a single web application, the default installed by
      // copying the context.xml file into the Engine/Host directory and calling it ROOT, which
      // tomcat assumes is the default webapp.
      project.copy {
        from("src/main/tomcat") {
          include "context.xml"
        }
        into "${project.buildDir}/tomcat/conf/Catalina/localhost/"
        rename {
          "${project.tomcatConfig.contextPath}.xml"
        }
      }

      // copy the server.xml if it exists
      if (new File("src/main/tomcat/server.xml").exists()) {
        project.copy {
          from("src/main/tomcat") {
            include "server.xml"
          }
          into "${project.buildDir}/tomcat/conf"
        }
      }

      // finally, copy the logging.properties file into WEB-INF/classes
      project.copy {
        from("src/main/resources") {
          include "logback.xml"
          include "logging.properties" //search-engine is still on jdk14 style logging
        }
        into "web/WEB-INF/classes"
      }
    }
  }

  /**
   * Configuration bean
   */
  class TomcatPluginConfiguration {
    def webappRoot = "src/main/webapp"
    def contextPath = "ROOT"
    def maxMemory = "256M"
  }
}