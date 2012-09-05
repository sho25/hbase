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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Common interface for models capable of supporting protobuf marshalling  * and unmarshalling. Hooks up to the ProtobufMessageBodyConsumer and  * ProtobufMessageBodyProducer adapters.   */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
interface|interface
name|ProtobufMessageHandler
block|{
comment|/**    * @return the protobuf represention of the model    */
specifier|public
name|byte
index|[]
name|createProtobufOutput
parameter_list|()
function_decl|;
comment|/**    * Initialize the model from a protobuf representation.    * @param message the raw bytes of the protobuf message    * @return reference to self for convenience    * @throws IOException    */
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
function_decl|;
block|}
end_interface

end_unit

