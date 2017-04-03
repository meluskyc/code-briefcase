import { CodeBriefcasePage } from './app.po';

describe('code-briefcase App', () => {
  let page: CodeBriefcasePage;

  beforeEach(() => {
    page = new CodeBriefcasePage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
