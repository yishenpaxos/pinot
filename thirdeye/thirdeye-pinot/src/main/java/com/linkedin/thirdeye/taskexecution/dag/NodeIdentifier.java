package com.linkedin.thirdeye.taskexecution.dag;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class NodeIdentifier {
  public static final String SEPARATOR = "::";
  private String namespace = "";
  private String name = "";
  private String fullName = "";

  public NodeIdentifier() {
  }

  public NodeIdentifier(String name) {
    setName(name);
  }

  public NodeIdentifier(String namespace, String name) {
    setNamespace(namespace);
    setName(name);
  }

  public void setName(String name) {
    Preconditions.checkNotNull(name);
    this.name = name;
    updateFullName();
  }

  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    Preconditions.checkNotNull(namespace);
    this.namespace = namespace;
    updateFullName();
  }

  private void updateFullName() {
    if (StringUtils.isNotBlank(name)) {
      if (StringUtils.isNotBlank(namespace)) {
        fullName = namespace + SEPARATOR + name;
      } else {
        fullName = name;
      }
    }
  }

  public String getFullName() {
    return fullName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeIdentifier that = (NodeIdentifier) o;
    return Objects.equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
