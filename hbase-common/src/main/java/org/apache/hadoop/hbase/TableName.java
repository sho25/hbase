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
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArraySet
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
name|KVComparator
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
name|hbase
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Immutable POJO class for representing a table name.  * Which is of the form:  *&lt;table namespace&gt;:&lt;table qualifier&gt;  *  * Two special namespaces:  *  * 1. hbase - system namespace, used to contain hbase internal tables  * 2. default - tables with no explicit specified namespace will  * automatically fall into this namespace.  *  * ie  *  * a) foo:bar, means namespace=foo and qualifier=bar  * b) bar, means namespace=default and qualifier=bar  * c) default:bar, means namespace=default and qualifier=bar  *  *<p>  * Internally, in this class, we cache the instances to limit the number of objects and  *  make the "equals" faster. We try to minimize the number of objects created of  *  the number of array copy to check if we already have an instance of this TableName. The code  *  is not optimize for a new instance creation but is optimized to check for existence.  *</p>  */
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
comment|/** See {@link #createTableNameIfNecessary(ByteBuffer, ByteBuffer)} */
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|TableName
argument_list|>
name|tableCache
init|=
operator|new
name|CopyOnWriteArraySet
argument_list|<
name|TableName
argument_list|>
argument_list|()
decl_stmt|;
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
comment|/** The hbase:meta table's name. */
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
specifier|public
specifier|static
specifier|final
name|String
name|OLD_META_STR
init|=
literal|".META."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OLD_ROOT_STR
init|=
literal|"-ROOT-"
decl_stmt|;
comment|/**    * TableName for old -ROOT- table. It is used to read/process old WALs which have    * ROOT edits.    */
specifier|public
specifier|static
specifier|final
name|TableName
name|OLD_ROOT_TABLE_NAME
init|=
name|getADummyTableName
argument_list|(
name|OLD_ROOT_STR
argument_list|)
decl_stmt|;
comment|/**    * TableName for old .META. table. Used in testing.    */
specifier|public
specifier|static
specifier|final
name|TableName
name|OLD_META_TABLE_NAME
init|=
name|getADummyTableName
argument_list|(
name|OLD_META_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|name
decl_stmt|;
specifier|private
specifier|final
name|String
name|nameAsString
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|namespace
decl_stmt|;
specifier|private
specifier|final
name|String
name|namespaceAsString
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
specifier|final
name|String
name|qualifierAsString
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|systemTable
decl_stmt|;
specifier|private
specifier|final
name|int
name|hashCode
decl_stmt|;
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
operator|<
literal|0
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
name|byte
index|[]
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
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|qualifierName
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|isLegalTableQualifierName
parameter_list|(
specifier|final
name|byte
index|[]
name|qualifierName
parameter_list|,
name|boolean
name|isSnapshot
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
argument_list|,
name|isSnapshot
argument_list|)
expr_stmt|;
return|return
name|qualifierName
return|;
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
name|isLegalTableQualifierName
argument_list|(
name|qualifierName
argument_list|,
name|start
argument_list|,
name|end
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|,
name|boolean
name|isSnapshot
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
name|isSnapshot
condition|?
literal|"Snapshot"
else|:
literal|"Table"
operator|+
literal|" qualifier must not be empty"
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
name|start
index|]
operator|+
literal|"> at 0. "
operator|+
operator|(
name|isSnapshot
condition|?
literal|"Snapshot"
else|:
literal|"User-space table"
operator|)
operator|+
literal|" qualifiers can only start with 'alphanumeric "
operator|+
literal|"characters': i.e. [a-zA-Z_0-9]: "
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
literal|"Illegal character code:"
operator|+
name|qualifierName
index|[
name|i
index|]
operator|+
literal|",<"
operator|+
operator|(
name|char
operator|)
name|qualifierName
index|[
name|i
index|]
operator|+
literal|"> at "
operator|+
name|i
operator|+
literal|". "
operator|+
operator|(
name|isSnapshot
condition|?
literal|"Snapshot"
else|:
literal|"User-space table"
operator|)
operator|+
literal|" qualifiers can only contain "
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
comment|/**    * Valid namespace characters are [a-zA-Z_0-9]    */
specifier|public
specifier|static
name|void
name|isLegalNamespaceName
parameter_list|(
specifier|final
name|byte
index|[]
name|namespaceName
parameter_list|,
specifier|final
name|int
name|start
parameter_list|,
specifier|final
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
literal|"Namespace name must not be empty"
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
name|start
argument_list|,
name|end
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
comment|/**    * Ideally, getNameAsString should contain namespace within it,    * but if the namespace is default, it just returns the name. This method    * takes care of this corner case.    */
specifier|public
name|String
name|getNameWithNamespaceInclAsString
parameter_list|()
block|{
if|if
condition|(
name|getNamespaceAsString
argument_list|()
operator|.
name|equals
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
argument_list|)
condition|)
block|{
return|return
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
operator|+
name|TableName
operator|.
name|NAMESPACE_DELIM
operator|+
name|getNameAsString
argument_list|()
return|;
block|}
return|return
name|getNameAsString
argument_list|()
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
comment|/**    *    * @throws IllegalArgumentException See {@link #valueOf(byte[])}    */
specifier|private
name|TableName
parameter_list|(
name|ByteBuffer
name|namespace
parameter_list|,
name|ByteBuffer
name|qualifier
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
operator|.
name|qualifier
operator|=
operator|new
name|byte
index|[
name|qualifier
operator|.
name|remaining
argument_list|()
index|]
expr_stmt|;
name|qualifier
operator|.
name|duplicate
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|qualifier
argument_list|)
expr_stmt|;
name|this
operator|.
name|qualifierAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|qualifier
argument_list|)
expr_stmt|;
if|if
condition|(
name|qualifierAsString
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
name|qualifierAsString
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
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME
argument_list|,
name|namespace
argument_list|)
condition|)
block|{
comment|// Using the same objects: this will make the comparison faster later
name|this
operator|.
name|namespace
operator|=
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME
expr_stmt|;
name|this
operator|.
name|namespaceAsString
operator|=
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
expr_stmt|;
name|this
operator|.
name|systemTable
operator|=
literal|false
expr_stmt|;
comment|// The name does not include the namespace when it's the default one.
name|this
operator|.
name|nameAsString
operator|=
name|qualifierAsString
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|this
operator|.
name|qualifier
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME
argument_list|,
name|namespace
argument_list|)
condition|)
block|{
name|this
operator|.
name|namespace
operator|=
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME
expr_stmt|;
name|this
operator|.
name|namespaceAsString
operator|=
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
expr_stmt|;
name|this
operator|.
name|systemTable
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|namespace
operator|=
operator|new
name|byte
index|[
name|namespace
operator|.
name|remaining
argument_list|()
index|]
expr_stmt|;
name|namespace
operator|.
name|duplicate
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|namespace
argument_list|)
expr_stmt|;
name|this
operator|.
name|namespaceAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|namespace
argument_list|)
expr_stmt|;
name|this
operator|.
name|systemTable
operator|=
literal|false
expr_stmt|;
block|}
name|this
operator|.
name|nameAsString
operator|=
name|namespaceAsString
operator|+
name|NAMESPACE_DELIM
operator|+
name|qualifierAsString
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|nameAsString
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|hashCode
operator|=
name|nameAsString
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|isLegalNamespaceName
argument_list|(
name|this
operator|.
name|namespace
argument_list|)
expr_stmt|;
name|isLegalTableQualifierName
argument_list|(
name|this
operator|.
name|qualifier
argument_list|)
expr_stmt|;
block|}
comment|/**    * This is only for the old and meta tables.    */
specifier|private
name|TableName
parameter_list|(
name|String
name|qualifier
parameter_list|)
block|{
name|this
operator|.
name|qualifier
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
name|this
operator|.
name|qualifierAsString
operator|=
name|qualifier
expr_stmt|;
name|this
operator|.
name|namespace
operator|=
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME
expr_stmt|;
name|this
operator|.
name|namespaceAsString
operator|=
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
expr_stmt|;
name|this
operator|.
name|systemTable
operator|=
literal|true
expr_stmt|;
comment|// WARNING: nameAsString is different than name for old meta& root!
comment|// This is by design.
name|this
operator|.
name|nameAsString
operator|=
name|namespaceAsString
operator|+
name|NAMESPACE_DELIM
operator|+
name|qualifierAsString
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|this
operator|.
name|qualifier
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|nameAsString
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
comment|/**    * Check that the object does not exist already. There are two reasons for creating the objects    * only once:    * 1) With 100K regions, the table names take ~20MB.    * 2) Equals becomes much faster as it's resolved with a reference and an int comparison.    */
specifier|private
specifier|static
name|TableName
name|createTableNameIfNecessary
parameter_list|(
name|ByteBuffer
name|bns
parameter_list|,
name|ByteBuffer
name|qns
parameter_list|)
block|{
for|for
control|(
name|TableName
name|tn
range|:
name|tableCache
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|qns
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getNamespace
argument_list|()
argument_list|,
name|bns
argument_list|)
condition|)
block|{
return|return
name|tn
return|;
block|}
block|}
name|TableName
name|newTable
init|=
operator|new
name|TableName
argument_list|(
name|bns
argument_list|,
name|qns
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableCache
operator|.
name|add
argument_list|(
name|newTable
argument_list|)
condition|)
block|{
comment|// Adds the specified element if it is not already present
return|return
name|newTable
return|;
block|}
comment|// Someone else added it. Let's find it.
for|for
control|(
name|TableName
name|tn
range|:
name|tableCache
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|qns
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getNamespace
argument_list|()
argument_list|,
name|bns
argument_list|)
condition|)
block|{
return|return
name|tn
return|;
block|}
block|}
comment|// this should never happen.
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|newTable
operator|+
literal|" was supposed to be in the cache"
argument_list|)
throw|;
block|}
comment|/**    * It is used to create table names for old META, and ROOT table.    * These tables are not really legal tables. They are not added into the cache.    * @return a dummy TableName instance (with no validation) for the passed qualifier    */
specifier|private
specifier|static
name|TableName
name|getADummyTableName
parameter_list|(
name|String
name|qualifier
parameter_list|)
block|{
return|return
operator|new
name|TableName
argument_list|(
name|qualifier
argument_list|)
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
for|for
control|(
name|TableName
name|tn
range|:
name|tableCache
control|)
block|{
if|if
condition|(
name|qualifierAsString
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getQualifierAsString
argument_list|()
argument_list|)
operator|&&
name|namespaceAsString
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getNameAsString
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|tn
return|;
block|}
block|}
return|return
name|createTableNameIfNecessary
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|namespaceAsString
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualifierAsString
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @throws IllegalArgumentException if fullName equals old root or old meta. Some code    *  depends on this. The test is buried in the table creation to save on array comparison    *  when we're creating a standard table object that will be in the cache.    */
specifier|public
specifier|static
name|TableName
name|valueOf
parameter_list|(
name|byte
index|[]
name|fullName
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
for|for
control|(
name|TableName
name|tn
range|:
name|tableCache
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getName
argument_list|()
argument_list|,
name|fullName
argument_list|)
condition|)
block|{
return|return
name|tn
return|;
block|}
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
name|fullName
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
operator|<
literal|0
condition|)
block|{
return|return
name|createTableNameIfNecessary
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|fullName
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|createTableNameIfNecessary
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|fullName
argument_list|,
literal|0
argument_list|,
name|namespaceDelimIndex
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|fullName
argument_list|,
name|namespaceDelimIndex
operator|+
literal|1
argument_list|,
name|fullName
operator|.
name|length
operator|-
operator|(
name|namespaceDelimIndex
operator|+
literal|1
operator|)
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * @throws IllegalArgumentException if fullName equals old root or old meta. Some code    *  depends on this.    */
specifier|public
specifier|static
name|TableName
name|valueOf
parameter_list|(
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|TableName
name|tn
range|:
name|tableCache
control|)
block|{
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getNameAsString
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|tn
return|;
block|}
block|}
name|int
name|namespaceDelimIndex
init|=
name|name
operator|.
name|indexOf
argument_list|(
name|NAMESPACE_DELIM
argument_list|)
decl_stmt|;
name|byte
index|[]
name|nameB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|namespaceDelimIndex
operator|<
literal|0
condition|)
block|{
return|return
name|createTableNameIfNecessary
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|nameB
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|createTableNameIfNecessary
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|nameB
argument_list|,
literal|0
argument_list|,
name|namespaceDelimIndex
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|nameB
argument_list|,
name|namespaceDelimIndex
operator|+
literal|1
argument_list|,
name|nameB
operator|.
name|length
operator|-
operator|(
name|namespaceDelimIndex
operator|+
literal|1
operator|)
argument_list|)
argument_list|)
return|;
block|}
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
for|for
control|(
name|TableName
name|tn
range|:
name|tableCache
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|qualifier
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|tn
operator|.
name|getNamespace
argument_list|()
argument_list|,
name|namespace
argument_list|)
condition|)
block|{
return|return
name|tn
return|;
block|}
block|}
return|return
name|createTableNameIfNecessary
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|namespace
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|qualifier
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TableName
name|valueOf
parameter_list|(
name|ByteBuffer
name|namespace
parameter_list|,
name|ByteBuffer
name|qualifier
parameter_list|)
block|{
if|if
condition|(
name|namespace
operator|==
literal|null
operator|||
name|namespace
operator|.
name|remaining
argument_list|()
operator|<
literal|1
condition|)
block|{
return|return
name|createTableNameIfNecessary
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME
argument_list|)
argument_list|,
name|qualifier
argument_list|)
return|;
block|}
return|return
name|createTableNameIfNecessary
argument_list|(
name|namespace
argument_list|,
name|qualifier
argument_list|)
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
return|return
name|o
operator|.
name|hashCode
argument_list|()
operator|==
name|hashCode
operator|&&
name|nameAsString
operator|.
name|equals
argument_list|(
name|tableName
operator|.
name|nameAsString
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashCode
return|;
block|}
comment|/**    * For performance reasons, the ordering is not lexicographic.    */
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
if|if
condition|(
name|this
operator|==
name|tableName
condition|)
return|return
literal|0
return|;
if|if
condition|(
name|this
operator|.
name|hashCode
operator|<
name|tableName
operator|.
name|hashCode
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|this
operator|.
name|hashCode
operator|>
name|tableName
operator|.
name|hashCode
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
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
name|KVComparator
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
return|;
block|}
return|return
name|KeyValue
operator|.
name|COMPARATOR
return|;
block|}
block|}
end_class

end_unit

