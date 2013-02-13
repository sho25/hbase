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
name|snapshot
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
comment|/**  * Utility class to help manage {@link SnapshotDescription SnapshotDesriptions}.  */
end_comment

begin_class
specifier|public
class|class
name|SnapshotDescriptionUtils
block|{
specifier|private
name|SnapshotDescriptionUtils
parameter_list|()
block|{
comment|// private constructor for utility class
block|}
comment|/**    * Check to make sure that the description of the snapshot requested is valid    * @param snapshot description of the snapshot    * @throws IllegalArgumentException if the name of the snapshot or the name of the table to    *           snapshot are not valid names.    */
specifier|public
specifier|static
name|void
name|assertSnapshotRequestIsValid
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
comment|// FIXME these method names is really bad - trunk will probably change
comment|// make sure the snapshot name is valid
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure the table name is valid
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

