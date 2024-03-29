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
operator|.
name|procedure2
operator|.
name|store
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|procedure2
operator|.
name|Procedure
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
name|procedure2
operator|.
name|ProcedureUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * when loading we will iterator the procedures twice, so use this class to cache the deserialized  * result to prevent deserializing multiple times.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ProtoAndProcedure
block|{
specifier|private
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proto
decl_stmt|;
specifier|private
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
decl_stmt|;
specifier|public
name|ProtoAndProcedure
parameter_list|(
name|ProcedureProtos
operator|.
name|Procedure
name|proto
parameter_list|)
block|{
name|this
operator|.
name|proto
operator|=
name|proto
expr_stmt|;
block|}
specifier|public
name|Procedure
argument_list|<
name|?
argument_list|>
name|getProcedure
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
name|proc
operator|=
name|ProcedureUtil
operator|.
name|convertToProcedure
argument_list|(
name|proto
argument_list|)
expr_stmt|;
block|}
return|return
name|proc
return|;
block|}
specifier|public
name|ProcedureProtos
operator|.
name|Procedure
name|getProto
parameter_list|()
block|{
return|return
name|proto
return|;
block|}
block|}
end_class

end_unit

