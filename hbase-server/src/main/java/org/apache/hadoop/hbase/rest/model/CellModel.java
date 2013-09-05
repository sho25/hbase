begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
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
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlAccessType
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
name|XmlAccessorType
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
name|XmlValue
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
name|KeyValue
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
name|rest
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
name|rest
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|CellMessage
operator|.
name|Cell
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

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonProperty
import|;
end_import

begin_comment
comment|/**  * Representation of a cell. A cell is a single value associated a column and  * optional qualifier, and either the timestamp when it was stored or the user-  * provided timestamp if one was explicitly supplied.  *  *<pre>  *&lt;complexType name="Cell"&gt;  *&lt;sequence&gt;  *&lt;element name="value" maxOccurs="1" minOccurs="1"&gt;  *&lt;simpleType&gt;  *&lt;restriction base="base64Binary"/&gt;  *&lt;/simpleType&gt;  *&lt;/element&gt;  *&lt;/sequence&gt;  *&lt;attribute name="column" type="base64Binary" /&gt;  *&lt;attribute name="timestamp" type="int" /&gt;  *&lt;/complexType&gt;  *</pre>  */
end_comment

begin_class
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"Cell"
argument_list|)
annotation|@
name|XmlAccessorType
argument_list|(
name|XmlAccessType
operator|.
name|FIELD
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CellModel
implements|implements
name|ProtobufMessageHandler
implements|,
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
annotation|@
name|JsonProperty
argument_list|(
literal|"column"
argument_list|)
annotation|@
name|XmlAttribute
specifier|private
name|byte
index|[]
name|column
decl_stmt|;
annotation|@
name|JsonProperty
argument_list|(
literal|"timestamp"
argument_list|)
annotation|@
name|XmlAttribute
specifier|private
name|long
name|timestamp
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
annotation|@
name|JsonProperty
argument_list|(
literal|"$"
argument_list|)
annotation|@
name|XmlValue
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
comment|/**    * Default constructor    */
specifier|public
name|CellModel
parameter_list|()
block|{}
comment|/**    * Constructor    * @param column    * @param value    */
specifier|public
name|CellModel
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|column
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param column    * @param qualifier    * @param value    */
specifier|public
name|CellModel
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|column
argument_list|,
name|qualifier
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor from KeyValue    * @param kv    */
specifier|public
name|CellModel
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
name|this
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param column    * @param timestamp    * @param value    */
specifier|public
name|CellModel
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * Constructor    * @param column    * @param qualifier    * @param timestamp    * @param value    */
specifier|public
name|CellModel
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|KeyValue
operator|.
name|makeColumn
argument_list|(
name|column
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * @return the column    */
specifier|public
name|byte
index|[]
name|getColumn
parameter_list|()
block|{
return|return
name|column
return|;
block|}
comment|/**    * @param column the column to set    */
specifier|public
name|void
name|setColumn
parameter_list|(
name|byte
index|[]
name|column
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
comment|/**    * @return true if the timestamp property has been specified by the    * user    */
specifier|public
name|boolean
name|hasUserTimestamp
parameter_list|()
block|{
return|return
name|timestamp
operator|!=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
return|;
block|}
comment|/**    * @return the timestamp    */
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**    * @param timestamp the timestamp to set    */
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
comment|/**    * @return the value    */
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**    * @param value the value to set    */
specifier|public
name|void
name|setValue
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
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
name|Cell
operator|.
name|Builder
name|builder
init|=
name|Cell
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setColumn
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|getColumn
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setData
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasUserTimestamp
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setTimestamp
argument_list|(
name|getTimestamp
argument_list|()
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
name|Cell
operator|.
name|Builder
name|builder
init|=
name|Cell
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
name|setColumn
argument_list|(
name|builder
operator|.
name|getColumn
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|setValue
argument_list|(
name|builder
operator|.
name|getData
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|hasTimestamp
argument_list|()
condition|)
block|{
name|setTimestamp
argument_list|(
name|builder
operator|.
name|getTimestamp
argument_list|()
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

