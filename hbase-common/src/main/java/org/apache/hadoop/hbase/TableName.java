begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
operator|.
name|KeyComparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Immutable POJO class for representing a table name.  * Which is of the form:  *&lt;table namespace&gt;:&lt;table qualifier&gt;  *  * Two special namespaces:  *  * 1. hbase - system namespace, used to contain hbase internal tables  * 2. default - tables with no explicit specified namespace will  * automatically fall into this namespace.  *  * ie  *  * a) foo:bar, means namespace=foo and qualifier=bar  * b) bar, means namespace=default and qualifier=bar  * c) default:bar, means namespace=default and qualifier=bar  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|TableName
implements|implements
name|Comparable
argument_list|<
name|TableName
argument_list|>
block|{
comment|/** Namespace delimiter */
comment|//this should always be only 1 byte long
specifier|public
specifier|final
specifier|static
name|char
name|NAMESPACE_DELIM
init|=
literal|':'
decl_stmt|;
comment|// A non-capture group so that this can be embedded.
comment|// regex is a bit more complicated to support nuance of tables
comment|// in default namespace
comment|//Allows only letters, digits and '_'
specifier|public
specifier|static
specifier|final
name|String
name|VALID_NAMESPACE_REGEX
init|=
literal|"(?:[a-zA-Z_0-9]+)"
decl_stmt|;
comment|//Allows only letters, digits, '_', '-' and '.'
specifier|public
specifier|static
specifier|final
name|String
name|VALID_TABLE_QUALIFIER_REGEX
init|=
literal|"(?:[a-zA-Z_0-9][a-zA-Z_0-9-.]*)"
decl_stmt|;
comment|//Concatenation of NAMESPACE_REGEX and TABLE_QUALIFIER_REGEX,
comment|//with NAMESPACE_DELIM as delimiter
specifier|public
specifier|static
specifier|final
name|String
name|VALID_USER_TABLE_REGEX
init|=
literal|"(?:(?:(?:"
operator|+
name|VALID_NAMESPACE_REGEX
operator|+
literal|"\\"
operator|+
name|NAMESPACE_DELIM
operator|+
literal|")?)"
operator|+
literal|"(?:"
operator|+
name|VALID_TABLE_QUALIFIER_REGEX
operator|+
literal|"))"
decl_stmt|;
comment|/** The META table's name. */
specifier|public
specifier|static
specifier|final
name|TableName
name|META_TABLE_NAME
init|=
name|valueOf
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|,
literal|"meta"
argument_list|)
decl_stmt|;
comment|/** The Namespace table's name. */
specifier|public
specifier|static
specifier|final
name|TableName
name|NAMESPACE_TABLE_NAME
init|=
name|valueOf
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|,
literal|"namespace"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OLD_META_STR
init|=
literal|".META."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OLD_ROOT_STR
init|=
literal|"-ROOT-"
decl_stmt|;
specifier|private
name|byte
index|[]
name|name
decl_stmt|;
specifier|private
name|String
name|nameAsString
decl_stmt|;
specifier|private
name|byte
index|[]
name|namespace
decl_stmt|;
specifier|private
name|String
name|namespaceAsString
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
name|String
name|qualifierAsString
decl_stmt|;
specifier|private
name|boolean
name|systemTable
decl_stmt|;
specifier|private
name|TableName
parameter_list|()
block|{}
comment|/**    * Check passed byte array, "tableName", is legal user-space table name.    * @return Returns passed<code>tableName</code> param    * @throws IllegalArgumentException if passed a tableName is null or    * is made of other than 'word' characters or underscores: i.e.    *<code>[a-zA-Z_0-9.-:]</code>. The ':' is used to delimit the namespace    * from the table name and can be used for nothing else.    *    * Namespace names can only contain 'word' characters    *<code>[a-zA-Z_0-9]</code> or '_'    *    * Qualifier names can only contain 'word' characters    *<code>[a-zA-Z_0-9]</code> or '_', '.' or '-'.    * The name may not start with '.' or '-'.    *    * Valid fully qualified table names:    * foo:bar, namespace=>foo, table=>bar    * org:foo.bar, namespace=org, table=>foo.bar    */
specifier|public
specifier|static
name|byte
index|[]
name|isLegalFullyQualifiedTableName
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
block|{
if|if
condition|(
name|tableName
operator|==
literal|null
operator|||
name|tableName
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Name is null or empty"
argument_list|)
throw|;
block|}
name|int
name|namespaceDelimIndex
init|=
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Bytes
operator|.
name|lastIndexOf
argument_list|(
name|tableName
argument_list|,
operator|(
name|byte
operator|)
name|NAMESPACE_DELIM
argument_list|)
decl_stmt|;
if|if
condition|(
name|namespaceDelimIndex
operator|==
literal|0
operator|||
name|namespaceDelimIndex
operator|==
operator|-
literal|1
condition|)
block|{
name|isLegalTableQualifierName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|isLegalNamespaceName
argument_list|(
name|tableName
argument_list|,
literal|0
argument_list|,
name|namespaceDelimIndex
argument_list|)
expr_stmt|;
name|isLegalTableQualifierName
argument_list|(
name|tableName
argument_list|,
name|namespaceDelimIndex
operator|+
literal|1
argument_list|,
name|tableName
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|tableName
return|;
block|}
specifier|public
specifier|static
name|void
name|isLegalTableQualifierName
parameter_list|(
specifier|final
name|byte
index|[]
name|qualifierName
parameter_list|)
block|{
name|isLegalTableQualifierName
argument_list|(
name|qualifierName
argument_list|,
literal|0
argument_list|,
name|qualifierName
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Qualifier names can only contain 'word' characters    *<code>[a-zA-Z_0-9]</code> or '_', '.' or '-'.    * The name may not start with '.' or '-'.    *    * @param qualifierName byte array containing the qualifier name    * @param start start index    * @param end end index (exclusive)    */
specifier|public
specifier|static
name|void
name|isLegalTableQualifierName
parameter_list|(
specifier|final
name|byte
index|[]
name|qualifierName
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
if|if
condition|(
name|end
operator|-
name|start
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Table qualifier must not be empty"
argument_list|)
throw|;
block|}
if|if
condition|(
name|qualifierName
index|[
name|start
index|]
operator|==
literal|'.'
operator|||
name|qualifierName
index|[
name|start
index|]
operator|==
literal|'-'
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal first character<"
operator|+
name|qualifierName
index|[
literal|0
index|]
operator|+
literal|"> at 0. Namespaces can only start with alphanumeric "
operator|+
literal|"characters': i.e. [a-zA-Z_0-9]: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifierName
argument_list|)
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|Character
operator|.
name|isLetterOrDigit
argument_list|(
name|qualifierName
index|[
name|i
index|]
argument_list|)
operator|||
name|qualifierName
index|[
name|i
index|]
operator|==
literal|'_'
operator|||
name|qualifierName
index|[
name|i
index|]
operator|==
literal|'-'
operator|||
name|qualifierName
index|[
name|i
index|]
operator|==
literal|'.'
condition|)
block|{
continue|continue;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal character<"
operator|+
name|qualifierName
index|[
name|i
index|]
operator|+
literal|"> at "
operator|+
name|i
operator|+
literal|". User-space table qualifiers can only contain "
operator|+
literal|"'alphanumeric characters': i.e. [a-zA-Z_0-9-.]: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifierName
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|isLegalNamespaceName
parameter_list|(
name|byte
index|[]
name|namespaceName
parameter_list|)
block|{
name|isLegalNamespaceName
argument_list|(
name|namespaceName
argument_list|,
literal|0
argument_list|,
name|namespaceName
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Valid namespace characters are [a-zA-Z_0-9]    * @param namespaceName    * @param offset    * @param length    */
specifier|public
specifier|static
name|void
name|isLegalNamespaceName
parameter_list|(
name|byte
index|[]
name|namespaceName
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|offset
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|Character
operator|.
name|isLetterOrDigit
argument_list|(
name|namespaceName
index|[
name|i
index|]
argument_list|)
operator|||
name|namespaceName
index|[
name|i
index|]
operator|==
literal|'_'
condition|)
block|{
continue|continue;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal character<"
operator|+
name|namespaceName
index|[
name|i
index|]
operator|+
literal|"> at "
operator|+
name|i
operator|+
literal|". Namespaces can only contain "
operator|+
literal|"'alphanumeric characters': i.e. [a-zA-Z_0-9]: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|namespaceName
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|public
name|byte
index|[]
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|String
name|getNameAsString
parameter_list|()
block|{
return|return
name|nameAsString
return|;
block|}
specifier|public
name|byte
index|[]
name|getNamespace
parameter_list|()
block|{
return|return
name|namespace
return|;
block|}
specifier|public
name|String
name|getNamespaceAsString
parameter_list|()
block|{
return|return
name|namespaceAsString
return|;
block|}
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|qualifier
return|;
block|}
specifier|public
name|String
name|getQualifierAsString
parameter_list|()
block|{
return|return
name|qualifierAsString
return|;
block|}
specifier|public
name|byte
index|[]
name|toBytes
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|boolean
name|isSystemTable
parameter_list|()
block|{
return|return
name|systemTable
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|nameAsString
return|;
block|}
specifier|public
specifier|static
name|TableName
name|valueOf
parameter_list|(
name|byte
index|[]
name|namespace
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|TableName
name|ret
init|=
operator|new
name|TableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespace
operator|==
literal|null
operator|||
name|namespace
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|namespace
operator|=
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME
expr_stmt|;
block|}
name|ret
operator|.
name|namespace
operator|=
name|namespace
expr_stmt|;
name|ret
operator|.
name|namespaceAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|namespace
argument_list|)
expr_stmt|;
name|ret
operator|.
name|qualifier
operator|=
name|qualifier
expr_stmt|;
name|ret
operator|.
name|qualifierAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
name|finishValueOf
argument_list|(
name|ret
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
specifier|static
name|TableName
name|valueOf
parameter_list|(
name|String
name|namespaceAsString
parameter_list|,
name|String
name|qualifierAsString
parameter_list|)
block|{
name|TableName
name|ret
init|=
operator|new
name|TableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespaceAsString
operator|==
literal|null
operator|||
name|namespaceAsString
operator|.
name|length
argument_list|()
operator|<
literal|1
condition|)
block|{
name|namespaceAsString
operator|=
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
expr_stmt|;
block|}
name|ret
operator|.
name|namespaceAsString
operator|=
name|namespaceAsString
expr_stmt|;
name|ret
operator|.
name|namespace
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|namespaceAsString
argument_list|)
expr_stmt|;
name|ret
operator|.
name|qualifier
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualifierAsString
argument_list|)
expr_stmt|;
name|ret
operator|.
name|qualifierAsString
operator|=
name|qualifierAsString
expr_stmt|;
name|finishValueOf
argument_list|(
name|ret
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|private
specifier|static
name|void
name|finishValueOf
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|isLegalNamespaceName
argument_list|(
name|tableName
operator|.
name|namespace
argument_list|)
expr_stmt|;
name|isLegalTableQualifierName
argument_list|(
name|tableName
operator|.
name|qualifier
argument_list|)
expr_stmt|;
name|tableName
operator|.
name|nameAsString
operator|=
name|createFullyQualified
argument_list|(
name|tableName
operator|.
name|namespaceAsString
argument_list|,
name|tableName
operator|.
name|qualifierAsString
argument_list|)
expr_stmt|;
name|tableName
operator|.
name|name
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
operator|.
name|nameAsString
argument_list|)
expr_stmt|;
name|tableName
operator|.
name|systemTable
operator|=
name|Bytes
operator|.
name|equals
argument_list|(
name|tableName
operator|.
name|namespace
argument_list|,
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|TableName
name|valueOf
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
block|{
return|return
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|name
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TableName
name|valueOf
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|OLD_ROOT_STR
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|OLD_ROOT_STR
operator|+
literal|" has been deprecated."
argument_list|)
throw|;
block|}
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|OLD_META_STR
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|OLD_META_STR
operator|+
literal|" no longer exists. The table has been "
operator|+
literal|"renamed to "
operator|+
name|META_TABLE_NAME
argument_list|)
throw|;
block|}
name|isLegalFullyQualifiedTableName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|index
init|=
name|name
operator|.
name|indexOf
argument_list|(
name|NAMESPACE_DELIM
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
return|return
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
argument_list|,
name|name
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
return|;
block|}
return|return
name|TableName
operator|.
name|valueOf
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|,
name|name
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|createFullyQualified
parameter_list|(
name|String
name|namespace
parameter_list|,
name|String
name|tableQualifier
parameter_list|)
block|{
if|if
condition|(
name|namespace
operator|.
name|equals
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|tableQualifier
return|;
block|}
return|return
name|namespace
operator|+
name|NAMESPACE_DELIM
operator|+
name|tableQualifier
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|TableName
name|tableName
init|=
operator|(
name|TableName
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|nameAsString
operator|.
name|equals
argument_list|(
name|tableName
operator|.
name|nameAsString
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|nameAsString
operator|.
name|hashCode
argument_list|()
decl_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|this
operator|.
name|nameAsString
operator|.
name|compareTo
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get the appropriate row comparator for this table.    *    * @return The comparator.    */
specifier|public
name|KeyComparator
name|getRowComparator
parameter_list|()
block|{
if|if
condition|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|equals
argument_list|(
name|this
argument_list|)
condition|)
block|{
return|return
name|KeyValue
operator|.
name|META_COMPARATOR
operator|.
name|getRawComparator
argument_list|()
return|;
block|}
return|return
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|getRawComparator
argument_list|()
return|;
block|}
block|}
end_class

end_unit

