begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|migration
operator|.
name|nineteen
operator|.
name|HStoreFileToStoreFile
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
name|util
operator|.
name|ProgramDriver
import|;
end_import

begin_comment
comment|/**  * Driver for hbase mapreduce jobs. Select which to run by passing  * name of job to this main.  */
end_comment

begin_class
specifier|public
class|class
name|Driver
block|{
comment|/**    * @param args    * @throws Throwable     */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|ProgramDriver
name|pgd
init|=
operator|new
name|ProgramDriver
argument_list|()
decl_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|RowCounter
operator|.
name|NAME
argument_list|,
name|RowCounter
operator|.
name|class
argument_list|,
literal|"Count rows in HBase table"
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|HStoreFileToStoreFile
operator|.
name|JOBNAME
argument_list|,
name|HStoreFileToStoreFile
operator|.
name|class
argument_list|,
literal|"Bulk convert 0.19 HStoreFiles to 0.20 StoreFiles"
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|driver
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

