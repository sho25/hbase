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
name|XmlElement
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
name|CellSetMessage
operator|.
name|CellSet
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
comment|/**  * Representation of a grouping of cells. May contain cells from more than  * one row. Encapsulates RowModel and CellModel models.  *   *<pre>  *&lt;complexType name="CellSet"&gt;  *&lt;sequence&gt;  *&lt;element name="row" type="tns:Row" maxOccurs="unbounded"   *       minOccurs="1"&gt;&lt;/element&gt;  *&lt;/sequence&gt;  *&lt;/complexType&gt;  *   *&lt;complexType name="Row"&gt;  *&lt;sequence&gt;  *&lt;element name="key" type="base64Binary"&gt;&lt;/element&gt;  *&lt;element name="cell" type="tns:Cell"   *       maxOccurs="unbounded" minOccurs="1"&gt;&lt;/element&gt;  *&lt;/sequence&gt;  *&lt;/complexType&gt;  *  *&lt;complexType name="Cell"&gt;  *&lt;sequence&gt;  *&lt;element name="value" maxOccurs="1" minOccurs="1"&gt;  *&lt;simpleType&gt;  *&lt;restriction base="base64Binary"/&gt;  *&lt;/simpleType&gt;  *&lt;/element&gt;  *&lt;/sequence&gt;  *&lt;attribute name="column" type="base64Binary" /&gt;  *&lt;attribute name="timestamp" type="int" /&gt;  *&lt;/complexType&gt;  *</pre>  */
end_comment

begin_class
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"CellSet"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CellSetModel
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
name|List
argument_list|<
name|RowModel
argument_list|>
name|rows
decl_stmt|;
comment|/**      * Constructor    */
specifier|public
name|CellSetModel
parameter_list|()
block|{
name|this
operator|.
name|rows
operator|=
operator|new
name|ArrayList
argument_list|<
name|RowModel
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param rows the rows    */
specifier|public
name|CellSetModel
parameter_list|(
name|List
argument_list|<
name|RowModel
argument_list|>
name|rows
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|rows
operator|=
name|rows
expr_stmt|;
block|}
comment|/**    * Add a row to this cell set    * @param row the row    */
specifier|public
name|void
name|addRow
parameter_list|(
name|RowModel
name|row
parameter_list|)
block|{
name|rows
operator|.
name|add
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the rows    */
annotation|@
name|XmlElement
argument_list|(
name|name
operator|=
literal|"Row"
argument_list|)
specifier|public
name|List
argument_list|<
name|RowModel
argument_list|>
name|getRows
parameter_list|()
block|{
return|return
name|rows
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
name|CellSet
operator|.
name|Builder
name|builder
init|=
name|CellSet
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|RowModel
name|row
range|:
name|getRows
argument_list|()
control|)
block|{
name|CellSet
operator|.
name|Row
operator|.
name|Builder
name|rowBuilder
init|=
name|CellSet
operator|.
name|Row
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|rowBuilder
operator|.
name|setKey
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|row
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|CellModel
name|cell
range|:
name|row
operator|.
name|getCells
argument_list|()
control|)
block|{
name|Cell
operator|.
name|Builder
name|cellBuilder
init|=
name|Cell
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|cellBuilder
operator|.
name|setColumn
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|cell
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|cellBuilder
operator|.
name|setData
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|cell
operator|.
name|hasUserTimestamp
argument_list|()
condition|)
block|{
name|cellBuilder
operator|.
name|setTimestamp
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rowBuilder
operator|.
name|addValues
argument_list|(
name|cellBuilder
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|addRows
argument_list|(
name|rowBuilder
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
name|CellSet
operator|.
name|Builder
name|builder
init|=
name|CellSet
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
for|for
control|(
name|CellSet
operator|.
name|Row
name|row
range|:
name|builder
operator|.
name|getRowsList
argument_list|()
control|)
block|{
name|RowModel
name|rowModel
init|=
operator|new
name|RowModel
argument_list|(
name|row
operator|.
name|getKey
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|row
operator|.
name|getValuesList
argument_list|()
control|)
block|{
name|long
name|timestamp
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
if|if
condition|(
name|cell
operator|.
name|hasTimestamp
argument_list|()
condition|)
block|{
name|timestamp
operator|=
name|cell
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
block|}
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|cell
operator|.
name|getColumn
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|timestamp
argument_list|,
name|cell
operator|.
name|getData
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|addRow
argument_list|(
name|rowModel
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

