package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class RepoHomePage {
    WebDriver driver;

    public RepoHomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = "strong[itemprop='name'] a")
    private WebElement repoTitleHeader;

    @FindBy(id = "settings-tab")
    private WebElement settingsTab;

    public String getRepoTitleText() {
        return repoTitleHeader.getText();
    }

    public void clickSettings() {
        settingsTab.click();
    }
}