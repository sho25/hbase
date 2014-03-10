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
name|util
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
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|HTable
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
name|Mutation
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
name|client
operator|.
name|Result
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
name|RetriesExhaustedWithDetailsException
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
name|security
operator|.
name|User
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
name|test
operator|.
name|LoadTestDataGenerator
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
name|security
operator|.
name|UserGroupInformation
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
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * A MultiThreadUpdater that helps to work with ACL  */
end_comment

begin_class
specifier|public
class|class
name|MultiThreadedUpdaterWithACL
extends|extends
name|MultiThreadedUpdater
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MultiThreadedUpdaterWithACL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|COMMA
init|=
literal|","
decl_stmt|;
specifier|private
name|User
name|userOwner
decl_stmt|;
comment|/**    * Maps user with Table instance. Because the table instance has to be created    * per user inorder to work in that user's context    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|HTable
argument_list|>
name|userVsTable
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HTable
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|User
argument_list|>
name|users
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|User
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|String
index|[]
name|userNames
decl_stmt|;
specifier|public
name|MultiThreadedUpdaterWithACL
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|double
name|updatePercent
parameter_list|,
name|User
name|userOwner
parameter_list|,
name|String
name|userNames
parameter_list|)
block|{
name|super
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
name|updatePercent
argument_list|)
expr_stmt|;
name|this
operator|.
name|userOwner
operator|=
name|userOwner
expr_stmt|;
name|this
operator|.
name|userNames
operator|=
name|userNames
operator|.
name|split
argument_list|(
name|COMMA
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addUpdaterThreads
parameter_list|(
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
operator|++
name|i
control|)
block|{
name|HBaseUpdaterThread
name|updater
init|=
operator|new
name|HBaseUpdaterThreadWithACL
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|updaters
operator|.
name|add
argument_list|(
name|updater
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
class|class
name|HBaseUpdaterThreadWithACL
extends|extends
name|HBaseUpdaterThread
block|{
specifier|private
name|HTable
name|table
decl_stmt|;
specifier|private
name|MutateAccessAction
name|mutateAction
init|=
operator|new
name|MutateAccessAction
argument_list|()
decl_stmt|;
specifier|public
name|HBaseUpdaterThreadWithACL
parameter_list|(
name|int
name|updaterId
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|updaterId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|HTable
name|createTable
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeHTable
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|HTable
name|table
range|:
name|userVsTable
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while closing the table "
operator|+
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while closing the HTable "
operator|+
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Result
name|getRow
parameter_list|(
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|long
name|rowKeyBase
parameter_list|,
specifier|final
name|byte
index|[]
name|cf
parameter_list|)
block|{
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Result
name|res
init|=
literal|null
decl_stmt|;
name|HTable
name|localTable
init|=
literal|null
decl_stmt|;
try|try
block|{
name|int
name|mod
init|=
operator|(
operator|(
name|int
operator|)
name|rowKeyBase
operator|%
name|userNames
operator|.
name|length
operator|)
decl_stmt|;
if|if
condition|(
name|userVsTable
operator|.
name|get
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
operator|==
literal|null
condition|)
block|{
name|localTable
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|userVsTable
operator|.
name|put
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|,
name|localTable
argument_list|)
expr_stmt|;
name|res
operator|=
name|localTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|localTable
operator|=
name|userVsTable
operator|.
name|get
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
expr_stmt|;
name|res
operator|=
name|localTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get the row for key = ["
operator|+
name|get
operator|.
name|getRow
argument_list|()
operator|+
literal|"], column family = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
operator|+
literal|"]"
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
block|}
decl_stmt|;
if|if
condition|(
name|userNames
operator|!=
literal|null
operator|&&
name|userNames
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|int
name|mod
init|=
operator|(
operator|(
name|int
operator|)
name|rowKeyBase
operator|%
name|userNames
operator|.
name|length
operator|)
decl_stmt|;
name|User
name|user
decl_stmt|;
if|if
condition|(
operator|!
name|users
operator|.
name|containsKey
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
condition|)
block|{
name|UserGroupInformation
name|realUserUgi
init|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
decl_stmt|;
name|user
operator|=
name|User
operator|.
name|create
argument_list|(
name|realUserUgi
argument_list|)
expr_stmt|;
name|users
operator|.
name|put
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|user
operator|=
name|users
operator|.
name|get
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Result
name|result
init|=
operator|(
name|Result
operator|)
name|user
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get the row for key = ["
operator|+
name|get
operator|.
name|getRow
argument_list|()
operator|+
literal|"], column family = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
operator|+
literal|"]"
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
comment|// This means that no users were present
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|mutate
parameter_list|(
specifier|final
name|HTable
name|table
parameter_list|,
name|Mutation
name|m
parameter_list|,
specifier|final
name|long
name|keyBase
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|cf
parameter_list|,
specifier|final
name|byte
index|[]
name|q
parameter_list|,
specifier|final
name|byte
index|[]
name|v
parameter_list|)
block|{
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|m
operator|=
name|dataGenerator
operator|.
name|beforeMutate
argument_list|(
name|keyBase
argument_list|,
name|m
argument_list|)
expr_stmt|;
name|mutateAction
operator|.
name|setMutation
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|mutateAction
operator|.
name|setCF
argument_list|(
name|cf
argument_list|)
expr_stmt|;
name|mutateAction
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|mutateAction
operator|.
name|setQualifier
argument_list|(
name|q
argument_list|)
expr_stmt|;
name|mutateAction
operator|.
name|setValue
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|mutateAction
operator|.
name|setStartTime
argument_list|(
name|start
argument_list|)
expr_stmt|;
name|mutateAction
operator|.
name|setKeyBase
argument_list|(
name|keyBase
argument_list|)
expr_stmt|;
name|userOwner
operator|.
name|runAs
argument_list|(
name|mutateAction
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|recordFailure
argument_list|(
name|m
argument_list|,
name|keyBase
argument_list|,
name|start
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|failedKeySet
operator|.
name|add
argument_list|(
name|keyBase
argument_list|)
expr_stmt|;
block|}
block|}
class|class
name|MutateAccessAction
implements|implements
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
name|HTable
name|table
decl_stmt|;
specifier|private
name|long
name|start
decl_stmt|;
specifier|private
name|Mutation
name|m
decl_stmt|;
specifier|private
name|long
name|keyBase
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|byte
index|[]
name|cf
decl_stmt|;
specifier|private
name|byte
index|[]
name|q
decl_stmt|;
specifier|private
name|byte
index|[]
name|v
decl_stmt|;
specifier|public
name|MutateAccessAction
parameter_list|()
block|{        }
specifier|public
name|void
name|setStartTime
parameter_list|(
specifier|final
name|long
name|start
parameter_list|)
block|{
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
block|}
specifier|public
name|void
name|setMutation
parameter_list|(
specifier|final
name|Mutation
name|m
parameter_list|)
block|{
name|this
operator|.
name|m
operator|=
name|m
expr_stmt|;
block|}
specifier|public
name|void
name|setRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
block|}
specifier|public
name|void
name|setCF
parameter_list|(
specifier|final
name|byte
index|[]
name|cf
parameter_list|)
block|{
name|this
operator|.
name|cf
operator|=
name|cf
expr_stmt|;
block|}
specifier|public
name|void
name|setQualifier
parameter_list|(
specifier|final
name|byte
index|[]
name|q
parameter_list|)
block|{
name|this
operator|.
name|q
operator|=
name|q
expr_stmt|;
block|}
specifier|public
name|void
name|setValue
parameter_list|(
specifier|final
name|byte
index|[]
name|v
parameter_list|)
block|{
name|this
operator|.
name|v
operator|=
name|v
expr_stmt|;
block|}
specifier|public
name|void
name|setKeyBase
parameter_list|(
specifier|final
name|long
name|keyBase
parameter_list|)
block|{
name|this
operator|.
name|keyBase
operator|=
name|keyBase
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|m
operator|instanceof
name|Increment
condition|)
block|{
name|table
operator|.
name|increment
argument_list|(
operator|(
name|Increment
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Append
condition|)
block|{
name|table
operator|.
name|append
argument_list|(
operator|(
name|Append
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Put
condition|)
block|{
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
name|v
argument_list|,
operator|(
name|Put
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Delete
condition|)
block|{
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
name|v
argument_list|,
operator|(
name|Delete
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unsupported mutation "
operator|+
name|m
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
name|totalOpTimeMs
operator|.
name|addAndGet
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|recordFailure
argument_list|(
name|m
argument_list|,
name|keyBase
argument_list|,
name|start
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|recordFailure
parameter_list|(
specifier|final
name|Mutation
name|m
parameter_list|,
specifier|final
name|long
name|keyBase
parameter_list|,
specifier|final
name|long
name|start
parameter_list|,
name|IOException
name|e
parameter_list|)
block|{
name|failedKeySet
operator|.
name|add
argument_list|(
name|keyBase
argument_list|)
expr_stmt|;
name|String
name|exceptionInfo
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|RetriesExhaustedWithDetailsException
condition|)
block|{
name|RetriesExhaustedWithDetailsException
name|aggEx
init|=
operator|(
name|RetriesExhaustedWithDetailsException
operator|)
name|e
decl_stmt|;
name|exceptionInfo
operator|=
name|aggEx
operator|.
name|getExhaustiveDescription
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|StringWriter
name|stackWriter
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
name|stackWriter
argument_list|)
decl_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|(
name|pw
argument_list|)
expr_stmt|;
name|pw
operator|.
name|flush
argument_list|()
expr_stmt|;
name|exceptionInfo
operator|=
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to mutate: "
operator|+
name|keyBase
operator|+
literal|" after "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|+
literal|"ms; region information: "
operator|+
name|getRegionDebugInfoSafe
argument_list|(
name|table
argument_list|,
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"; errors: "
operator|+
name|exceptionInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

