begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|TableName
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|SnapshotProtos
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
comment|/**  * Class to help with dealing with a snapshot description on the client side.  * There is a corresponding class on the server side.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ClientSnapshotDescriptionUtils
block|{
comment|/**    * Check to make sure that the description of the snapshot requested is valid    * @param snapshot description of the snapshot    * @throws IllegalArgumentException if the name of the snapshot or the name of the table to    *           snapshot are not valid names.    */
specifier|public
specifier|static
name|void
name|assertSnapshotRequestIsValid
parameter_list|(
name|SnapshotProtos
operator|.
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
comment|// make sure the snapshot name is valid
name|TableName
operator|.
name|isLegalTableQualifierName
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
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|snapshot
operator|.
name|hasTable
argument_list|()
condition|)
block|{
comment|// make sure the table name is valid, this will implicitly check validity
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableName
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"System table snapshots are not allowed"
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Returns a single line (no \n) representation of snapshot metadata.  Use this instead of    * {@link org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription#toString()}.  We don't replace SnapshotDescrpition's toString    * because it is auto-generated by protoc.    * @param ssd    * @return Single line string with a summary of the snapshot parameters    */
specifier|public
specifier|static
name|String
name|toString
parameter_list|(
name|SnapshotProtos
operator|.
name|SnapshotDescription
name|ssd
parameter_list|)
block|{
if|if
condition|(
name|ssd
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
literal|"{ ss="
operator|+
name|ssd
operator|.
name|getName
argument_list|()
operator|+
literal|" table="
operator|+
operator|(
name|ssd
operator|.
name|hasTable
argument_list|()
condition|?
name|TableName
operator|.
name|valueOf
argument_list|(
name|ssd
operator|.
name|getTable
argument_list|()
argument_list|)
else|:
literal|""
operator|)
operator|+
literal|" type="
operator|+
name|ssd
operator|.
name|getType
argument_list|()
operator|+
literal|" }"
return|;
block|}
block|}
end_class

end_unit

