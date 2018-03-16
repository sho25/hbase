begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
import|import static
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
name|AbstractHBaseTool
operator|.
name|EXIT_FAILURE
import|;
end_import

begin_import
import|import static
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
name|AbstractHBaseTool
operator|.
name|EXIT_SUCCESS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|HBaseConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Option
import|;
end_import

begin_class
specifier|public
class|class
name|AbstractHBaseToolTest
block|{
specifier|static
specifier|final
class|class
name|Options
block|{
specifier|static
specifier|final
name|Option
name|REQUIRED
init|=
operator|new
name|Option
argument_list|(
literal|null
argument_list|,
literal|"required"
argument_list|,
literal|true
argument_list|,
literal|""
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Option
name|OPTIONAL
init|=
operator|new
name|Option
argument_list|(
literal|null
argument_list|,
literal|"optional"
argument_list|,
literal|true
argument_list|,
literal|""
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Option
name|BOOLEAN
init|=
operator|new
name|Option
argument_list|(
literal|null
argument_list|,
literal|"boolean"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
decl_stmt|;
block|}
comment|/**    * Simple tool to test options parsing.    * 3 options: required, optional, and boolean    * 2 deprecated options to test backward compatibility: -opt (old version of --optional) and    * -bool (old version of --boolean).    */
specifier|private
specifier|static
class|class
name|TestTool
extends|extends
name|AbstractHBaseTool
block|{
name|String
name|requiredValue
decl_stmt|;
name|String
name|optionalValue
decl_stmt|;
name|boolean
name|booleanValue
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addRequiredOption
argument_list|(
name|Options
operator|.
name|REQUIRED
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|Options
operator|.
name|OPTIONAL
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|Options
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|requiredValue
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|Options
operator|.
name|REQUIRED
operator|.
name|getLongOpt
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|Options
operator|.
name|OPTIONAL
operator|.
name|getLongOpt
argument_list|()
argument_list|)
condition|)
block|{
name|optionalValue
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|Options
operator|.
name|OPTIONAL
operator|.
name|getLongOpt
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|booleanValue
operator|=
name|booleanValue
operator|||
name|cmd
operator|.
name|hasOption
argument_list|(
name|Options
operator|.
name|BOOLEAN
operator|.
name|getLongOpt
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOldArgs
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|invalidArgs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|args
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
name|cmd
init|=
name|args
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-opt"
argument_list|)
condition|)
block|{
name|optionalValue
operator|=
name|args
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-bool"
argument_list|)
condition|)
block|{
name|booleanValue
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|invalidArgs
operator|.
name|add
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
block|}
block|}
name|args
operator|.
name|addAll
argument_list|(
name|invalidArgs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|EXIT_SUCCESS
return|;
block|}
block|}
name|TestTool
name|tool
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|tool
operator|=
operator|new
name|TestTool
argument_list|()
expr_stmt|;
name|tool
operator|.
name|setConf
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAllOptionsSet
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"--required=foo"
block|,
literal|"--optional=bar"
block|,
literal|"--boolean"
block|}
decl_stmt|;
name|int
name|returnValue
init|=
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXIT_SUCCESS
argument_list|,
name|returnValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|tool
operator|.
name|requiredValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|tool
operator|.
name|optionalValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tool
operator|.
name|booleanValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOptionsNotSet
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"--required=foo"
block|}
decl_stmt|;
name|int
name|returnValue
init|=
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXIT_SUCCESS
argument_list|,
name|returnValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|tool
operator|.
name|requiredValue
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|tool
operator|.
name|optionalValue
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tool
operator|.
name|booleanValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMissingRequiredOption
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[
literal|0
index|]
decl_stmt|;
name|int
name|returnValue
init|=
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXIT_FAILURE
argument_list|,
name|returnValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailureOnUnrecognizedOption
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"--required=foo"
block|,
literal|"-asdfs"
block|}
decl_stmt|;
name|int
name|returnValue
init|=
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXIT_FAILURE
argument_list|,
name|returnValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOldOptionsWork
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"--required=foo"
block|,
literal|"-opt"
block|,
literal|"bar"
block|,
literal|"-bool"
block|}
decl_stmt|;
name|int
name|returnValue
init|=
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXIT_SUCCESS
argument_list|,
name|returnValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|tool
operator|.
name|requiredValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|tool
operator|.
name|optionalValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tool
operator|.
name|booleanValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNewOptionOverridesOldOption
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"--required=foo"
block|,
literal|"--optional=baz"
block|,
literal|"-opt"
block|,
literal|"bar"
block|,
literal|"-bool"
block|}
decl_stmt|;
name|int
name|returnValue
init|=
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXIT_SUCCESS
argument_list|,
name|returnValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|tool
operator|.
name|requiredValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"baz"
argument_list|,
name|tool
operator|.
name|optionalValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tool
operator|.
name|booleanValue
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

