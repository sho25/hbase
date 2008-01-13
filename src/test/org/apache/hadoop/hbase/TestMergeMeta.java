begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/** Tests region merging */
end_comment

begin_class
specifier|public
class|class
name|TestMergeMeta
extends|extends
name|AbstractMergeTestBase
block|{
comment|/** constructor */
specifier|public
name|TestMergeMeta
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|1
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * test case    * @throws IOException    */
specifier|public
name|void
name|testMergeMeta
parameter_list|()
throws|throws
name|IOException
block|{
name|assertNotNull
argument_list|(
name|dfsCluster
argument_list|)
expr_stmt|;
name|HMerge
operator|.
name|merge
argument_list|(
name|conf
argument_list|,
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

