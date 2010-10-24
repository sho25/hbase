begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|Get
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
name|Scan
import|;
end_import

begin_comment
comment|/**  * Special internal-only scanner, currently used for increment operations to  * allow additional server-side arguments for Scan operations.  *<p>  * Rather than adding new options/parameters to the public Scan API, this new  * class has been created.  *<p>  * Supports adding an option to only read from the MemStore with  * {@link #checkOnlyMemStore()} or to only read from StoreFiles with  * {@link #checkOnlyStoreFiles()}.  */
end_comment

begin_class
class|class
name|InternalScan
extends|extends
name|Scan
block|{
specifier|private
name|boolean
name|memOnly
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|filesOnly
init|=
literal|false
decl_stmt|;
comment|/**    * @param get get to model scan after    */
specifier|public
name|InternalScan
parameter_list|(
name|Get
name|get
parameter_list|)
block|{
name|super
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
comment|/**    * StoreFiles will not be scanned. Only MemStore will be scanned.    */
specifier|public
name|void
name|checkOnlyMemStore
parameter_list|()
block|{
name|memOnly
operator|=
literal|true
expr_stmt|;
name|filesOnly
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * MemStore will not be scanned. Only StoreFiles will be scanned.    */
specifier|public
name|void
name|checkOnlyStoreFiles
parameter_list|()
block|{
name|memOnly
operator|=
literal|false
expr_stmt|;
name|filesOnly
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Returns true if only the MemStore should be checked.  False if not.    * @return true to only check MemStore    */
specifier|public
name|boolean
name|isCheckOnlyMemStore
parameter_list|()
block|{
return|return
operator|(
name|memOnly
operator|)
return|;
block|}
comment|/**    * Returns true if only StoreFiles should be checked.  False if not.    * @return true if only check StoreFiles    */
specifier|public
name|boolean
name|isCheckOnlyStoreFiles
parameter_list|()
block|{
return|return
operator|(
name|filesOnly
operator|)
return|;
block|}
block|}
end_class

end_unit

