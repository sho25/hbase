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
name|List
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
name|TableInfoMessage
operator|.
name|TableInfo
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_comment
comment|/**  * Representation of a list of table regions.   *   *<pre>  *&lt;complexType name="TableInfo"&gt;  *&lt;sequence&gt;  *&lt;element name="region" type="tns:TableRegion"   *       maxOccurs="unbounded" minOccurs="1"&gt;&lt;/element&gt;  *&lt;/sequence&gt;  *&lt;attribute name="name" type="string"&gt;&lt;/attribute&gt;  *&lt;/complexType&gt;  *</pre>  */
end_comment

begin_class
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"TableInfo"
argument_list|)
annotation|@
name|XmlType
argument_list|(
name|propOrder
operator|=
block|{
literal|"name"
block|,
literal|"regions"
block|}
argument_list|)
specifier|public
class|class
name|TableInfoModel
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
name|String
name|name
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TableRegionModel
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|TableRegionModel
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Default constructor    */
specifier|public
name|TableInfoModel
parameter_list|()
block|{}
comment|/**    * Constructor    * @param name    */
specifier|public
name|TableInfoModel
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
comment|/**    * Add a region model to the list    * @param region the region    */
specifier|public
name|void
name|add
parameter_list|(
name|TableRegionModel
name|region
parameter_list|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param index the index    * @return the region model    */
specifier|public
name|TableRegionModel
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|regions
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
comment|/**    * @return the regions    */
annotation|@
name|XmlElement
argument_list|(
name|name
operator|=
literal|"Region"
argument_list|)
specifier|public
name|List
argument_list|<
name|TableRegionModel
argument_list|>
name|getRegions
parameter_list|()
block|{
return|return
name|regions
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
comment|/**    * @param regions the regions to set    */
specifier|public
name|void
name|setRegions
parameter_list|(
name|List
argument_list|<
name|TableRegionModel
argument_list|>
name|regions
parameter_list|)
block|{
name|this
operator|.
name|regions
operator|=
name|regions
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
for|for
control|(
name|TableRegionModel
name|aRegion
range|:
name|regions
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|aRegion
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|createProtobufOutput
parameter_list|()
block|{
name|TableInfo
operator|.
name|Builder
name|builder
init|=
name|TableInfo
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
name|TableRegionModel
name|aRegion
range|:
name|regions
control|)
block|{
name|TableInfo
operator|.
name|Region
operator|.
name|Builder
name|regionBuilder
init|=
name|TableInfo
operator|.
name|Region
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|regionBuilder
operator|.
name|setName
argument_list|(
name|aRegion
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|regionBuilder
operator|.
name|setId
argument_list|(
name|aRegion
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|regionBuilder
operator|.
name|setStartKey
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|aRegion
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|regionBuilder
operator|.
name|setEndKey
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|aRegion
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|regionBuilder
operator|.
name|setLocation
argument_list|(
name|aRegion
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addRegions
argument_list|(
name|regionBuilder
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
name|TableInfo
operator|.
name|Builder
name|builder
init|=
name|TableInfo
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
name|TableInfo
operator|.
name|Region
name|region
range|:
name|builder
operator|.
name|getRegionsList
argument_list|()
control|)
block|{
name|add
argument_list|(
operator|new
name|TableRegionModel
argument_list|(
name|builder
operator|.
name|getName
argument_list|()
argument_list|,
name|region
operator|.
name|getId
argument_list|()
argument_list|,
name|region
operator|.
name|getStartKey
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|region
operator|.
name|getEndKey
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|region
operator|.
name|getLocation
argument_list|()
argument_list|)
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

