begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
operator|.
name|policies
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
name|client
operator|.
name|Append
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
name|client
operator|.
name|Delete
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
name|client
operator|.
name|Increment
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
name|client
operator|.
name|Put
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

begin_class
specifier|public
class|class
name|BaseViolationPolicyEnforcement
block|{
specifier|static
specifier|final
name|Append
name|APPEND
init|=
operator|new
name|Append
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Delete
name|DELETE
init|=
operator|new
name|Delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Increment
name|INCREMENT
init|=
operator|new
name|Increment
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Put
name|PUT
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
block|}
end_class

end_unit

