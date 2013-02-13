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
name|server
operator|.
name|snapshot
operator|.
name|error
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
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
import|;
end_import

begin_comment
comment|/**  * Generic running snapshot failure listener  */
end_comment

begin_interface
specifier|public
interface|interface
name|SnapshotFailureListener
block|{
comment|/**    * Notification that a given snapshot failed because of an error on the local server    * @param snapshot snapshot that failed    * @param reason explanation of why the snapshot failed    */
specifier|public
name|void
name|snapshotFailure
parameter_list|(
name|String
name|reason
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|)
function_decl|;
comment|/**    * Notification that a given snapshot failed because of an error on the local server    * @param reason reason the snapshot failed    * @param snapshot the snapshot that failed    * @param t the exception that caused the failure    */
specifier|public
name|void
name|snapshotFailure
parameter_list|(
name|String
name|reason
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Exception
name|t
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

