FROM jboss/wildfly:25.0.0.Final
RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#7rules --silent
ADD --chmod=0755 wildfly-init-config.sh /opt/jboss/wildfly/bin
CMD ["/opt/jboss/wildfly/bin/wildfly-init-config.sh"]