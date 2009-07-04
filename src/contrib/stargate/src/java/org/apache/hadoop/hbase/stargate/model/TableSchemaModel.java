begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|stargate
operator|.
name|model
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlAnyAttribute
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlAttribute
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlElement
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlRootElement
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlType
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|namespace
operator|.
name|QName
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
name|HColumnDescriptor
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
name|HConstants
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
name|HTableDescriptor
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
name|stargate
operator|.
name|ProtobufMessageHandler
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ColumnSchemaMessage
operator|.
name|ColumnSchema
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableSchemaMessage
operator|.
name|TableSchema
import|;
end_import

begin_comment
comment|/**  * A representation of HBase table descriptors.  *   *<pre>  *&lt;complexType name="TableSchema"&gt;  *&lt;sequence&gt;  *&lt;element name="column" type="tns:ColumnSchema"   *       maxOccurs="unbounded" minOccurs="1"&gt;&lt;/element&gt;  *&lt;/sequence&gt;  *&lt;attribute name="name" type="string"&gt;&lt;/attribute&gt;  *&lt;anyAttribute&gt;&lt;/anyAttribute&gt;  *&lt;/complexType&gt;  *</pre>  */
end_comment

begin_class
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"TableSchema"
argument_list|)
annotation|@
name|XmlType
argument_list|(
name|propOrder
operator|=
block|{
literal|"name"
block|,
literal|"columns"
block|}
argument_list|)
specifier|public
class|class
name|TableSchemaModel
implements|implements
name|Serializable
implements|,
name|ProtobufMessageHandler
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|QName
name|IS_META
init|=
operator|new
name|QName
argument_list|(
name|HTableDescriptor
operator|.
name|IS_META
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|QName
name|IS_ROOT
init|=
operator|new
name|QName
argument_list|(
name|HTableDescriptor
operator|.
name|IS_ROOT
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|QName
name|READONLY
init|=
operator|new
name|QName
argument_list|(
name|HTableDescriptor
operator|.
name|READONLY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|QName
name|TTL
init|=
operator|new
name|QName
argument_list|(
name|HColumnDescriptor
operator|.
name|TTL
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|QName
name|VERSIONS
init|=
operator|new
name|QName
argument_list|(
name|HConstants
operator|.
name|VERSIONS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|QName
name|COMPRESSION
init|=
operator|new
name|QName
argument_list|(
name|HColumnDescriptor
operator|.
name|COMPRESSION
argument_list|)
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|attrs
init|=
operator|new
name|HashMap
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ColumnSchemaModel
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnSchemaModel
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Default constructor.    */
specifier|public
name|TableSchemaModel
parameter_list|()
block|{}
comment|/**    * Add an attribute to the table descriptor    * @param name attribute name    * @param value attribute value    */
specifier|public
name|void
name|addAttribute
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|attrs
operator|.
name|put
argument_list|(
operator|new
name|QName
argument_list|(
name|name
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return a table descriptor value as a string. Calls toString() on the    * object stored in the descriptor value map.    * @param name the attribute name    * @return the attribute value    */
specifier|public
name|String
name|getAttribute
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Object
name|o
init|=
name|attrs
operator|.
name|get
argument_list|(
operator|new
name|QName
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|o
operator|!=
literal|null
condition|?
name|o
operator|.
name|toString
argument_list|()
else|:
literal|null
return|;
block|}
comment|/**    * Add a column family to the table descriptor    * @param object the column family model    */
specifier|public
name|void
name|addColumnFamily
parameter_list|(
name|ColumnSchemaModel
name|family
parameter_list|)
block|{
name|columns
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
comment|/**    * Retrieve the column family at the given index from the table descriptor    * @param index the index    * @return the column family model    */
specifier|public
name|ColumnSchemaModel
name|getColumnFamily
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|columns
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
comment|/**    * @return the table name    */
annotation|@
name|XmlAttribute
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * @return the map for holding unspecified (user) attributes    */
annotation|@
name|XmlAnyAttribute
specifier|public
name|Map
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|getAny
parameter_list|()
block|{
return|return
name|attrs
return|;
block|}
comment|/**    * @return the columns    */
annotation|@
name|XmlElement
argument_list|(
name|name
operator|=
literal|"ColumnSchema"
argument_list|)
specifier|public
name|List
argument_list|<
name|ColumnSchemaModel
argument_list|>
name|getColumns
parameter_list|()
block|{
return|return
name|columns
return|;
block|}
comment|/**    * @param name the table name    */
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**    * @param columns the columns to set    */
specifier|public
name|void
name|setColumns
parameter_list|(
name|List
argument_list|<
name|ColumnSchemaModel
argument_list|>
name|columns
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{ NAME=> '"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|attrs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getLocalPart
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" => '"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|", COLUMNS => [ "
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|ColumnSchemaModel
argument_list|>
name|i
init|=
name|columns
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|i
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ColumnSchemaModel
name|family
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|family
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"] }"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|// getters and setters for common schema attributes
comment|// cannot be standard bean type getters and setters, otherwise this would
comment|// confuse JAXB
comment|/**    * @return true if IS_META attribute exists and is truel    */
specifier|public
name|boolean
name|__getIsMeta
parameter_list|()
block|{
name|Object
name|o
init|=
name|attrs
operator|.
name|get
argument_list|(
name|IS_META
argument_list|)
decl_stmt|;
return|return
name|o
operator|!=
literal|null
condition|?
name|Boolean
operator|.
name|valueOf
argument_list|(
name|o
operator|.
name|toString
argument_list|()
argument_list|)
else|:
literal|false
return|;
block|}
comment|/**    * @return true if IS_ROOT attribute exists and is truel    */
specifier|public
name|boolean
name|__getIsRoot
parameter_list|()
block|{
name|Object
name|o
init|=
name|attrs
operator|.
name|get
argument_list|(
name|IS_ROOT
argument_list|)
decl_stmt|;
return|return
name|o
operator|!=
literal|null
condition|?
name|Boolean
operator|.
name|valueOf
argument_list|(
name|o
operator|.
name|toString
argument_list|()
argument_list|)
else|:
literal|false
return|;
block|}
comment|/**    * @return true if READONLY attribute exists and is truel    */
specifier|public
name|boolean
name|__getReadOnly
parameter_list|()
block|{
name|Object
name|o
init|=
name|attrs
operator|.
name|get
argument_list|(
name|READONLY
argument_list|)
decl_stmt|;
return|return
name|o
operator|!=
literal|null
condition|?
name|Boolean
operator|.
name|valueOf
argument_list|(
name|o
operator|.
name|toString
argument_list|()
argument_list|)
else|:
name|HTableDescriptor
operator|.
name|DEFAULT_READONLY
return|;
block|}
comment|/**    * @param value desired value of IS_META attribute    */
specifier|public
name|void
name|__setIsMeta
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|attrs
operator|.
name|put
argument_list|(
name|IS_META
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param value desired value of IS_ROOT attribute    */
specifier|public
name|void
name|__setIsRoot
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|attrs
operator|.
name|put
argument_list|(
name|IS_ROOT
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param value desired value of READONLY attribute    */
specifier|public
name|void
name|__setReadOnly
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|attrs
operator|.
name|put
argument_list|(
name|READONLY
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|createProtobufOutput
parameter_list|()
block|{
name|TableSchema
operator|.
name|Builder
name|builder
init|=
name|TableSchema
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|attrs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableSchema
operator|.
name|Attribute
operator|.
name|Builder
name|attrBuilder
init|=
name|TableSchema
operator|.
name|Attribute
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|attrBuilder
operator|.
name|setName
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getLocalPart
argument_list|()
argument_list|)
expr_stmt|;
name|attrBuilder
operator|.
name|setValue
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addAttrs
argument_list|(
name|attrBuilder
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ColumnSchemaModel
name|family
range|:
name|columns
control|)
block|{
name|Map
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|familyAttrs
init|=
name|family
operator|.
name|getAny
argument_list|()
decl_stmt|;
name|ColumnSchema
operator|.
name|Builder
name|familyBuilder
init|=
name|ColumnSchema
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|familyBuilder
operator|.
name|setName
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|familyAttrs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ColumnSchema
operator|.
name|Attribute
operator|.
name|Builder
name|attrBuilder
init|=
name|ColumnSchema
operator|.
name|Attribute
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|attrBuilder
operator|.
name|setName
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getLocalPart
argument_list|()
argument_list|)
expr_stmt|;
name|attrBuilder
operator|.
name|setValue
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|familyBuilder
operator|.
name|addAttrs
argument_list|(
name|attrBuilder
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|familyAttrs
operator|.
name|containsKey
argument_list|(
name|TTL
argument_list|)
condition|)
block|{
name|familyBuilder
operator|.
name|setTtl
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|familyAttrs
operator|.
name|get
argument_list|(
name|TTL
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|familyAttrs
operator|.
name|containsKey
argument_list|(
name|VERSIONS
argument_list|)
condition|)
block|{
name|familyBuilder
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|familyAttrs
operator|.
name|get
argument_list|(
name|VERSIONS
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|familyAttrs
operator|.
name|containsKey
argument_list|(
name|COMPRESSION
argument_list|)
condition|)
block|{
name|familyBuilder
operator|.
name|setCompression
argument_list|(
name|familyAttrs
operator|.
name|get
argument_list|(
name|COMPRESSION
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|addColumns
argument_list|(
name|familyBuilder
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|attrs
operator|.
name|containsKey
argument_list|(
name|READONLY
argument_list|)
condition|)
block|{
name|builder
operator|.
name|setReadOnly
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|attrs
operator|.
name|get
argument_list|(
name|READONLY
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProtobufMessageHandler
name|getObjectFromMessage
parameter_list|(
name|byte
index|[]
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSchema
operator|.
name|Builder
name|builder
init|=
name|TableSchema
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|mergeFrom
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|this
operator|.
name|setName
argument_list|(
name|builder
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TableSchema
operator|.
name|Attribute
name|attr
range|:
name|builder
operator|.
name|getAttrsList
argument_list|()
control|)
block|{
name|this
operator|.
name|addAttribute
argument_list|(
name|attr
operator|.
name|getName
argument_list|()
argument_list|,
name|attr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|hasReadOnly
argument_list|()
condition|)
block|{
name|this
operator|.
name|addAttribute
argument_list|(
name|HTableDescriptor
operator|.
name|READONLY
argument_list|,
name|builder
operator|.
name|getReadOnly
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ColumnSchema
name|family
range|:
name|builder
operator|.
name|getColumnsList
argument_list|()
control|)
block|{
name|ColumnSchemaModel
name|familyModel
init|=
operator|new
name|ColumnSchemaModel
argument_list|()
decl_stmt|;
name|familyModel
operator|.
name|setName
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ColumnSchema
operator|.
name|Attribute
name|attr
range|:
name|family
operator|.
name|getAttrsList
argument_list|()
control|)
block|{
name|familyModel
operator|.
name|addAttribute
argument_list|(
name|attr
operator|.
name|getName
argument_list|()
argument_list|,
name|attr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|family
operator|.
name|hasTtl
argument_list|()
condition|)
block|{
name|familyModel
operator|.
name|addAttribute
argument_list|(
name|HColumnDescriptor
operator|.
name|TTL
argument_list|,
name|family
operator|.
name|getTtl
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|family
operator|.
name|hasMaxVersions
argument_list|()
condition|)
block|{
name|familyModel
operator|.
name|addAttribute
argument_list|(
name|HConstants
operator|.
name|VERSIONS
argument_list|,
name|family
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|family
operator|.
name|hasCompression
argument_list|()
condition|)
block|{
name|familyModel
operator|.
name|addAttribute
argument_list|(
name|HColumnDescriptor
operator|.
name|COMPRESSION
argument_list|,
name|family
operator|.
name|getCompression
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|addColumnFamily
argument_list|(
name|familyModel
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

