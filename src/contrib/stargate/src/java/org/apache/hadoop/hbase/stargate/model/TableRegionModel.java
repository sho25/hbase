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
name|Serializable
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
comment|/**  * Representation of a region of a table and its current location on the  * storage cluster.  *   *<pre>  *&lt;complexType name="TableRegion"&gt;  *&lt;attribute name="name" type="string"&gt;&lt;/attribute&gt;  *&lt;attribute name="id" type="int"&gt;&lt;/attribute&gt;  *&lt;attribute name="startKey" type="base64Binary"&gt;&lt;/attribute&gt;  *&lt;attribute name="endKey" type="base64Binary"&gt;&lt;/attribute&gt;  *&lt;attribute name="location" type="string"&gt;&lt;/attribute&gt;  *&lt;/complexType&gt;  *</pre>  */
end_comment

begin_class
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"Region"
argument_list|)
annotation|@
name|XmlType
argument_list|(
name|propOrder
operator|=
block|{
literal|"name"
block|,
literal|"id"
block|,
literal|"startKey"
block|,
literal|"endKey"
block|,
literal|"location"
block|}
argument_list|)
specifier|public
class|class
name|TableRegionModel
implements|implements
name|Serializable
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
name|String
name|table
decl_stmt|;
specifier|private
name|long
name|id
decl_stmt|;
specifier|private
name|byte
index|[]
name|startKey
decl_stmt|;
specifier|private
name|byte
index|[]
name|endKey
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
comment|/**    * Constructor    */
specifier|public
name|TableRegionModel
parameter_list|()
block|{}
comment|/**    * Constructor    * @param table the table name    * @param id the encoded id of the region    * @param startKey the start key of the region    * @param endKey the end key of the region    * @param location the name and port of the region server hosting the region    */
specifier|public
name|TableRegionModel
parameter_list|(
name|String
name|table
parameter_list|,
name|long
name|id
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
name|this
operator|.
name|endKey
operator|=
name|endKey
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
comment|/**    * @return the region name    */
annotation|@
name|XmlAttribute
specifier|public
name|String
name|getName
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|startKey
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|id
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @return the encoded region id    */
annotation|@
name|XmlAttribute
specifier|public
name|long
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**    * @return the start key    */
annotation|@
name|XmlAttribute
specifier|public
name|byte
index|[]
name|getStartKey
parameter_list|()
block|{
return|return
name|startKey
return|;
block|}
comment|/**    * @return the end key    */
annotation|@
name|XmlAttribute
specifier|public
name|byte
index|[]
name|getEndKey
parameter_list|()
block|{
return|return
name|endKey
return|;
block|}
comment|/**    * @return the name and port of the region server hosting the region    */
annotation|@
name|XmlAttribute
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
comment|/**    * @param name region printable name    */
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|String
name|split
index|[]
init|=
name|name
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|table
operator|=
name|split
index|[
literal|0
index|]
expr_stmt|;
name|startKey
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|split
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|id
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|split
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param id the region's encoded id    */
specifier|public
name|void
name|setId
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
comment|/**    * @param startKey the start key    */
specifier|public
name|void
name|setStartKey
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|)
block|{
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
block|}
comment|/**    * @param endKey the end key    */
specifier|public
name|void
name|setEndKey
parameter_list|(
name|byte
index|[]
name|endKey
parameter_list|)
block|{
name|this
operator|.
name|endKey
operator|=
name|endKey
expr_stmt|;
block|}
comment|/**    * @param location the name and port of the region server hosting the region    */
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
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
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" [\n  id="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\n  startKey='"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|startKey
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"'\n  endKey='"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|endKey
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"'\n  location='"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"'\n]\n"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

