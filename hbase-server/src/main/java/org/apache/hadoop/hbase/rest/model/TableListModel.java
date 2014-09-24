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
name|XmlElementRef
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
name|TableListMessage
operator|.
name|TableList
import|;
end_import

begin_comment
comment|/**  * Simple representation of a list of table names.  */
end_comment

begin_class
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"TableList"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableListModel
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
name|TableModel
argument_list|>
name|tables
init|=
operator|new
name|ArrayList
argument_list|<
name|TableModel
argument_list|>
argument_list|()
decl_stmt|;
comment|/** 	 * Default constructor 	 */
specifier|public
name|TableListModel
parameter_list|()
block|{}
comment|/** 	 * Add the table name model to the list 	 * @param table the table model 	 */
specifier|public
name|void
name|add
parameter_list|(
name|TableModel
name|table
parameter_list|)
block|{
name|tables
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|/** 	 * @param index the index 	 * @return the table model 	 */
specifier|public
name|TableModel
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|tables
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
comment|/** 	 * @return the tables 	 */
annotation|@
name|XmlElementRef
argument_list|(
name|name
operator|=
literal|"table"
argument_list|)
specifier|public
name|List
argument_list|<
name|TableModel
argument_list|>
name|getTables
parameter_list|()
block|{
return|return
name|tables
return|;
block|}
comment|/** 	 * @param tables the tables to set 	 */
specifier|public
name|void
name|setTables
parameter_list|(
name|List
argument_list|<
name|TableModel
argument_list|>
name|tables
parameter_list|)
block|{
name|this
operator|.
name|tables
operator|=
name|tables
expr_stmt|;
block|}
comment|/* (non-Javadoc) 	 * @see java.lang.Object#toString() 	 */
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
name|TableModel
name|aTable
range|:
name|tables
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|aTable
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
name|TableList
operator|.
name|Builder
name|builder
init|=
name|TableList
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|TableModel
name|aTable
range|:
name|tables
control|)
block|{
name|builder
operator|.
name|addName
argument_list|(
name|aTable
operator|.
name|getName
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
name|TableList
operator|.
name|Builder
name|builder
init|=
name|TableList
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
name|String
name|table
range|:
name|builder
operator|.
name|getNameList
argument_list|()
control|)
block|{
name|this
operator|.
name|add
argument_list|(
operator|new
name|TableModel
argument_list|(
name|table
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

